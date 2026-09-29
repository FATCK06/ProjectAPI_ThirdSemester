// Gera docs/pdf/*.pdf a partir dos .md. O .md e a fonte: edite ele e rode de novo.
//
// Converte o Markdown em HTML (marked) e imprime com o Chrome/Edge em modo
// headless, que ja existe na maquina de todo mundo - sem precisar de LaTeX nem
// de pandoc.

import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { Marked } from 'marked';

const DOCS = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const SAIDA = join(DOCS, 'pdf');

const DOCUMENTOS = [
    {
        saida: 'manual-do-usuario.pdf',
        titulo: 'Manual do Usuário',
        arquivos: ['manual-usuario/README.md'],
    },
    {
        saida: 'rotas-api.pdf',
        titulo: 'Rotas da API',
        arquivos: ['api/README.md', 'api/ms-usuarios.md', 'api/ms-frota.md', 'api/ms-operacoes.md'],
    },
];

const NAVEGADORES = [
    process.env.CHROME_PATH,
    'C:/Program Files/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    'C:/Program Files/Microsoft/Edge/Application/msedge.exe',
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    '/usr/bin/google-chrome',
    '/usr/bin/chromium',
    '/usr/bin/chromium-browser',
].filter(Boolean);

/** Mesmo formato de ancora do GitHub, para os links do sumario funcionarem no PDF. */
function slug(texto) {
    return texto
        .toLowerCase()
        .replace(/<[^>]+>/g, '')
        .replace(/[^\p{L}\p{N}\s_-]/gu, '')
        .trim()
        .replace(/\s/g, '-');
}

function idDoArquivo(arquivo) {
    return 'doc-' + arquivo.replace(/[\/.]/g, '-');
}

function montarMarked() {
    const vistos = new Map();
    const marked = new Marked({ gfm: true });

    marked.use({
        renderer: {
            heading({ tokens, depth }) {
                const html = this.parser.parseInline(tokens);
                let id = slug(html);
                const repeticoes = vistos.get(id) ?? 0;
                vistos.set(id, repeticoes + 1);
                if (repeticoes > 0) id += '-' + repeticoes;
                return `<h${depth} id="${id}">${html}</h${depth}>\n`;
            },
            link({ href, title, tokens }) {
                const texto = this.parser.parseInline(tokens);
                // Link para outro .md que tambem esta no PDF vira ancora interna.
                const interno = href.match(/^\.\/([\w-]+\.md)(#.*)?$/);
                let destino = href;
                if (interno) {
                    destino = interno[2] ?? '#' + idDoArquivo('api/' + interno[1]);
                }
                const t = title ? ` title="${title}"` : '';
                return `<a href="${destino}"${t}>${texto}</a>`;
            },
        },
    });
    return marked;
}

function montarHtml(doc) {
    const marked = montarMarked();
    const corpo = doc.arquivos
        .map((arquivo) => {
            const md = readFileSync(join(DOCS, arquivo), 'utf8').replace(/<!--[\s\S]*?-->/g, '');
            return `<section id="${idDoArquivo(arquivo)}" class="arquivo">${marked.parse(md)}</section>`;
        })
        .join('\n');

    const logo = pathToFileURL(join(DOCS, 'img', 'rubyfox.png')).href;
    const hoje = new Date().toLocaleDateString('pt-BR');

    return `<!doctype html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<title>${doc.titulo}</title>
<style>${readFileSync(join(DOCS, 'scripts', 'estilo-pdf.css'), 'utf8')}</style>
</head>
<body>
<section class="capa">
  <img src="${logo}" alt="RubyFox">
  <h1>${doc.titulo}</h1>
  <p class="subtitulo">Controle Simplificado dos Motoristas Agregados</p>
  <p class="meta">Parceiro acadêmico: Newelog<br>Equipe RubyFox · FATEC São José dos Campos<br>Gerado em ${hoje}</p>
</section>
${corpo}
</body>
</html>`;
}

function acharNavegador() {
    const caminho = NAVEGADORES.find((c) => existsSync(c));
    if (!caminho) {
        console.error('Chrome ou Edge nao encontrado. Defina CHROME_PATH com o caminho do executavel.');
        process.exit(1);
    }
    return caminho;
}

const navegador = acharNavegador();
mkdirSync(SAIDA, { recursive: true });

for (const doc of DOCUMENTOS) {
    const html = join(SAIDA, doc.saida.replace('.pdf', '.tmp.html'));
    const pdf = join(SAIDA, doc.saida);
    writeFileSync(html, montarHtml(doc), 'utf8');

    execFileSync(navegador, [
        '--headless=new',
        '--disable-gpu',
        '--no-pdf-header-footer',
        '--allow-file-access-from-files',
        `--print-to-pdf=${pdf}`,
        pathToFileURL(html).href,
    ], { stdio: 'ignore' });

    // MANTER_HTML=1 deixa o .html ao lado do PDF, para inspecionar o layout.
    if (!process.env.MANTER_HTML) rmSync(html);
    console.log('gerado:', pdf);
}
