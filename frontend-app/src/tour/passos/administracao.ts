import type { MontarPassos, PassoTour } from './tipos';

const CADASTRO = '/usuarios/novo';
const STATUS = '/status';

/**
 * Telas de administração: só entram para quem tem a permissão (as rotas também
 * são barradas por RotaComPermissao e pelo gateway). Regras conferidas em
 * pages/Usuarios/Cadastro, pages/Status e Permissao.java.
 * O tour não preenche nem envia o formulário e não clica em "Atualizar".
 */
export const passosAdministracao: MontarPassos = ({ gerenciarUsuarios, verStatus }) => {
    const passos: PassoTour[] = [];

    if (gerenciarUsuarios) {
        passos.push(
            {
                id: 'cadastro-pagina',
                rota: CADASTRO,
                alvo: 'cadastro-pagina',
                menu: 'expandido',
                titulo: '"Cadastrar usuário"',
                texto:
                    'Cria o acesso de uma nova pessoa ao sistema. A senha é criptografada pelo servidor antes de ser ' +
                    'gravada.',
            },
            {
                id: 'cadastro-nome-email',
                rota: CADASTRO,
                alvo: 'cadastro-email',
                lado: 'right',
                titulo: '"Nome" e "E-mail"',
                texto: 'O nome aparece no rodapé do menu de quem entrar; o e-mail é o usado no login.',
            },
            {
                id: 'cadastro-senha',
                rota: CADASTRO,
                alvo: 'cadastro-senha',
                lado: 'right',
                titulo: '"Senha"',
                texto: 'Mínimo de 4 caracteres. O ícone de olho mostra ou oculta o que foi digitado.',
            },
            {
                id: 'cadastro-perfil',
                rota: CADASTRO,
                alvo: 'cadastro-perfil',
                lado: 'right',
                titulo: '"Perfil de acesso"',
                texto:
                    'Operador (padrão) vê só os dados operacionais. Gestor vê também os financeiros. Administrador ' +
                    'vê tudo, cadastra usuários e acessa o status dos serviços. O cadastro só acontece em "Cadastrar".',
            },
        );
    }

    if (verStatus) {
        passos.push(
            {
                id: 'status-pagina',
                rota: STATUS,
                alvo: 'status-cabecalho',
                menu: 'expandido',
                lado: 'bottom',
                titulo: '"Status dos serviços"',
                texto:
                    'Consulta a saúde de cada serviço ao abrir a tela e de novo a cada 30 s, com o horário da última ' +
                    'consulta. "Atualizar" consulta na hora.',
            },
            {
                id: 'status-lista',
                rota: STATUS,
                alvo: 'status-pagina',
                titulo: '"No ar" e "Fora"',
                texto:
                    'Cada serviço mostra o tempo de resposta e o estado. O resumo diz "Todos os serviços estão no ar" ' +
                    'ou "Há serviço fora do ar". Serviço que responde mas não alcança o banco aparece como "Fora".',
            },
        );
    }

    return passos;
};
