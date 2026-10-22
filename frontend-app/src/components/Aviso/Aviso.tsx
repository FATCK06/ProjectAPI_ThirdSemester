import { Info } from "lucide-react";
import "./aviso.css";

// Textos da história "Rentabilidade das operações": iguais em todas as telas.
export const AVISO_SEM_PERMISSAO_FINANCEIRO = "Você não tem permissão para visualizar os dados financeiros.";
export const AVISO_CUSTOS_NAO_INFORMADOS = "Custos não informados para este período.";
export const AVISO_SEM_VIAGENS_MEDIA = "Sem viagens no período. Não é possível calcular a média por viagem.";

interface PropsAviso {
    mensagem: string;
}

export function Aviso({ mensagem }: PropsAviso) {
    return (
        <p className="ui-aviso" role="status">
            <Info size={16} aria-hidden="true" />
            {mensagem}
        </p>
    );
}
