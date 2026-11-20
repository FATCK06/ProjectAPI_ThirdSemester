import { useState, type FormEvent, type ReactNode } from 'react';
import { Eye, EyeOff, Lock, Mail, ShieldCheck, User, UserPlus, type LucideIcon } from 'lucide-react';
import { criarUsuario, mensagemDeErro } from '../../../services/usuarios';
import { PERFIS, rotuloPerfil, type Perfil } from '../../../services/auth';
import { useToast } from '../../../components/Toast';
import { PageHeader } from '../../../components/PageHeader';
import './cadastro.css';

// Limites iguais aos do backend (CriarUsuarioRequest e UsuarioService)
const MAXIMO_TEXTO = 100;
const MINIMO_SENHA = 4;
const MAXIMO_SENHA = 72;
const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** O que cada perfil enxerga, no mesmo vocabulário das telas. Espelha PERMISSOES em auth.ts. */
const DESCRICAO_PERFIL: Record<Perfil, string> = {
    OPERADOR: 'Indicadores operacionais, importações e cadastros da frota. Não vê valores financeiros.',
    GESTOR: 'Tudo do operador, mais fretes, custos, rentabilidade e margem.',
    ADMINISTRADOR: 'Acesso total: cadastra usuários e acompanha o status dos serviços.',
};

type Campo = 'nome' | 'email' | 'senha';

const CAMPOS: Campo[] = ['nome', 'email', 'senha'];
const VAZIO: Record<Campo, string> = { nome: '', email: '', senha: '' };
const NADA_TOCADO: Record<Campo, boolean> = { nome: false, email: false, senha: false };

function validar(campo: Campo, valor: string): string | null {
    const texto = valor.trim();
    if (!texto) return 'Campo obrigatório.';
    if (campo === 'email' && !EMAIL_VALIDO.test(texto)) return 'Informe um e-mail válido, como nome@empresa.com.';
    if (campo === 'senha' && valor.length < MINIMO_SENHA) return `A senha precisa de pelo menos ${MINIMO_SENHA} caracteres.`;
    return null;
}

interface PropsCampoTexto {
    id: Campo;
    rotulo: string;
    Icone: LucideIcon;
    erro: string | null;
    ajuda?: string;
    children: ReactNode;
    acao?: ReactNode;
}

function CampoTexto({ id, rotulo, Icone, erro, ajuda, children, acao }: PropsCampoTexto) {
    return (
        <div className="cadastro-campo" data-erro={erro ? true : undefined} data-tour={`cadastro-${id}`}>
            <label htmlFor={id}>{rotulo}</label>
            <div className="cadastro-entrada">
                <Icone className="cadastro-entrada-icone" size={17} aria-hidden="true" />
                {children}
                {acao}
            </div>
            {erro ? (
                <span id={`${id}-mensagem`} className="cadastro-mensagem-erro" role="alert">{erro}</span>
            ) : (
                ajuda && <span id={`${id}-mensagem`} className="cadastro-ajuda">{ajuda}</span>
            )}
        </div>
    );
}

export function CadastroUsuario() {
    const [valores, setValores] = useState<Record<Campo, string>>(VAZIO);
    // O erro só aparece depois que o usuário sai do campo ou tenta enviar
    const [tocados, setTocados] = useState<Record<Campo, boolean>>(NADA_TOCADO);
    const [perfil, setPerfil] = useState<Perfil>('OPERADOR');
    const [mostrarSenha, setMostrarSenha] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const toast = useToast();

    const erros: Record<Campo, string | null> = {
        nome: validar('nome', valores.nome),
        email: validar('email', valores.email),
        senha: validar('senha', valores.senha),
    };
    const erroVisivel = (campo: Campo) => (tocados[campo] ? erros[campo] : null);
    const temAlgoPreenchido = Object.values(valores).some(Boolean) || perfil !== 'OPERADOR';

    function alterar(campo: Campo, valor: string) {
        setValores((atual) => ({ ...atual, [campo]: valor }));
    }

    function tocar(campo: Campo) {
        setTocados((atual) => ({ ...atual, [campo]: true }));
    }

    function limpar() {
        setValores(VAZIO);
        setTocados(NADA_TOCADO);
        setPerfil('OPERADOR');
        setMostrarSenha(false);
    }

    async function enviar(evento: FormEvent<HTMLFormElement>) {
        evento.preventDefault();
        setTocados({ nome: true, email: true, senha: true });

        const primeiroInvalido = CAMPOS.find((c) => erros[c]);
        if (primeiroInvalido) {
            document.getElementById(primeiroInvalido)?.focus();
            return;
        }

        setEnviando(true);
        try {
            const usuario = await criarUsuario({
                nome: valores.nome.trim(),
                email: valores.email.trim(),
                senha: valores.senha,
                perfilAcesso: perfil,
            });
            toast.sucesso(`${usuario.nome} cadastrado como ${rotuloPerfil(usuario.perfilAcesso)}. Já pode fazer login.`, {
                titulo: 'Usuário cadastrado',
            });
            limpar();
        } catch (falha) {
            // Mantém o formulário preenchido para o usuário só corrigir o que falhou.
            toast.erro(mensagemDeErro(falha), { titulo: 'Não foi possível cadastrar' });
        } finally {
            setEnviando(false);
        }
    }

    return (
        <div className="cadastro-pagina" data-tour="cadastro-pagina">
            <PageHeader titulo="Cadastrar usuário" subtitulo="Crie o acesso de um novo membro da equipe" />

            <form onSubmit={enviar} className="cadastro-card" noValidate data-tour="cadastro-form">
                <div className="cadastro-card-topo">
                    <span className="cadastro-card-icone"><UserPlus size={20} /></span>
                    <div>
                        <h2>Dados de acesso</h2>
                        <p>A senha é criptografada pelo servidor antes de ser gravada.</p>
                    </div>
                </div>

                <div className="cadastro-grade">
                    <CampoTexto id="nome" rotulo="Nome" Icone={User} erro={erroVisivel('nome')}>
                        <input
                            id="nome"
                            value={valores.nome}
                            onChange={(e) => alterar('nome', e.target.value)}
                            onBlur={() => tocar('nome')}
                            placeholder="Nome completo"
                            autoComplete="off"
                            maxLength={MAXIMO_TEXTO}
                            aria-invalid={Boolean(erroVisivel('nome'))}
                            aria-describedby="nome-mensagem"
                        />
                    </CampoTexto>

                    <CampoTexto id="email" rotulo="E-mail" Icone={Mail} erro={erroVisivel('email')}>
                        <input
                            id="email"
                            type="email"
                            value={valores.email}
                            onChange={(e) => alterar('email', e.target.value)}
                            onBlur={() => tocar('email')}
                            placeholder="nome@empresa.com"
                            autoComplete="off"
                            maxLength={MAXIMO_TEXTO}
                            aria-invalid={Boolean(erroVisivel('email'))}
                            aria-describedby="email-mensagem"
                        />
                    </CampoTexto>

                    <CampoTexto
                        id="senha"
                        rotulo="Senha inicial"
                        Icone={Lock}
                        erro={erroVisivel('senha')}
                        ajuda={`Mínimo de ${MINIMO_SENHA} caracteres. Repasse ao usuário por um canal seguro.`}
                        acao={(
                            <button
                                type="button"
                                className="cadastro-olho"
                                onClick={() => setMostrarSenha((v) => !v)}
                                aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                                aria-pressed={mostrarSenha}
                                title={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                            >
                                {mostrarSenha ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        )}
                    >
                        <input
                            id="senha"
                            type={mostrarSenha ? 'text' : 'password'}
                            value={valores.senha}
                            onChange={(e) => alterar('senha', e.target.value)}
                            onBlur={() => tocar('senha')}
                            placeholder="Digite a senha"
                            autoComplete="new-password"
                            maxLength={MAXIMO_SENHA}
                            aria-invalid={Boolean(erroVisivel('senha'))}
                            aria-describedby="senha-mensagem"
                        />
                    </CampoTexto>
                </div>

                <fieldset className="cadastro-perfis" data-tour="cadastro-perfil">
                    <legend>
                        <ShieldCheck size={16} aria-hidden="true" />
                        Perfil de acesso
                    </legend>
                    <div className="cadastro-perfis-opcoes">
                        {PERFIS.map((p) => (
                            <label key={p} className="cadastro-perfil" data-selecionado={perfil === p || undefined}>
                                <input
                                    type="radio"
                                    name="perfil"
                                    value={p}
                                    checked={perfil === p}
                                    onChange={() => setPerfil(p)}
                                />
                                <span className="cadastro-perfil-nome">{rotuloPerfil(p)}</span>
                                <span className="cadastro-perfil-descricao">{DESCRICAO_PERFIL[p]}</span>
                            </label>
                        ))}
                    </div>
                </fieldset>

                <div className="cadastro-acoes">
                    <button
                        type="button"
                        className="cadastro-botao-secundario"
                        onClick={limpar}
                        disabled={enviando || !temAlgoPreenchido}
                    >
                        Limpar
                    </button>
                    <button type="submit" className="cadastro-botao-primario" disabled={enviando}>
                        {enviando && <span className="cadastro-spinner" aria-hidden="true" />}
                        {enviando ? 'Cadastrando...' : 'Cadastrar usuário'}
                    </button>
                </div>
            </form>
        </div>
    );
}
