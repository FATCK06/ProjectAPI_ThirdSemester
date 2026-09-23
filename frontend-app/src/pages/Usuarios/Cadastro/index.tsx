import { useState } from 'react';
import { Eye, EyeOff, CheckCircle2, AlertCircle } from 'lucide-react';
import {
    criarUsuario,
    mensagemDeErro,
    PERFIS,
    type Perfil,
    type UsuarioResponse,
} from '../../../services/usuarios';
import './cadastro.css';

export function CadastroUsuario() {
    const [nome, setNome] = useState('');
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [perfil, setPerfil] = useState<Perfil>('Operador');
    const [mostrarSenha, setMostrarSenha] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState<string | null>(null);
    const [criado, setCriado] = useState<UsuarioResponse | null>(null);

    async function enviar(evento: React.FormEvent) {
        evento.preventDefault();
        setEnviando(true);
        setErro(null);
        setCriado(null);

        try {
            const usuario = await criarUsuario({ nome, email, senha, perfilAcesso: perfil });
            setCriado(usuario);
            setNome('');
            setEmail('');
            setSenha('');
            setPerfil('Operador');
        } catch (falha) {
            setErro(mensagemDeErro(falha));
        } finally {
            setEnviando(false);
        }
    }

    return (
        <div className="cadastro-container">
            <div className="cadastro-header">
                <h2>Cadastrar usuário</h2>
                <p>A senha é criptografada pelo servidor antes de ser gravada.</p>
            </div>

            <form onSubmit={enviar} className="cadastro-form">
                <div className="campo">
                    <label htmlFor="nome">Nome</label>
                    <input
                        id="nome"
                        value={nome}
                        onChange={(e) => setNome(e.target.value)}
                        placeholder="Nome completo"
                        required
                    />
                </div>

                <div className="campo">
                    <label htmlFor="email">E-mail</label>
                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="nome@empresa.com"
                        required
                    />
                </div>

                <div className="campo">
                    <label htmlFor="senha">Senha</label>
                    <div className="campo-senha">
                        <input
                            id="senha"
                            type={mostrarSenha ? 'text' : 'password'}
                            value={senha}
                            onChange={(e) => setSenha(e.target.value)}
                            placeholder="mínimo 4 caracteres"
                            minLength={4}
                            required
                        />
                        <button
                            type="button"
                            className="btn-olho"
                            onClick={() => setMostrarSenha((v) => !v)}
                            title={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                        >
                            {mostrarSenha ? <EyeOff size={20} /> : <Eye size={20} />}
                        </button>
                    </div>
                </div>

                <div className="campo">
                    <label htmlFor="perfil">Perfil de acesso</label>
                    <select id="perfil" value={perfil} onChange={(e) => setPerfil(e.target.value as Perfil)}>
                        {PERFIS.map((p) => (
                            <option key={p} value={p}>
                                {p}
                            </option>
                        ))}
                    </select>
                    <span className="campo-ajuda">
                        Administrador enxerga a página de status dos serviços.
                    </span>
                </div>

                {erro && (
                    <div className="alerta alerta-erro">
                        <AlertCircle size={18} />
                        <span>{erro}</span>
                    </div>
                )}

                {criado && (
                    <div className="alerta alerta-sucesso">
                        <CheckCircle2 size={18} />
                        <span>
                            <strong>{criado.nome}</strong> cadastrado como {criado.perfilAcesso}. Já pode fazer login.
                        </span>
                    </div>
                )}

                <button type="submit" className="btn-cadastrar" disabled={enviando}>
                    {enviando ? 'Cadastrando...' : 'Cadastrar'}
                </button>
            </form>
        </div>
    );
}
