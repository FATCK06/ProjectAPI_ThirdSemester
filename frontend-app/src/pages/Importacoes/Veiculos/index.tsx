import { Truck } from 'lucide-react';
import { ImportacaoWizard } from '../Manifestos';
import { Step4RevisaoVeiculos } from './components/Step4RevisaoVeiculos';
import { ResultadoTratamento } from './components/ResultadoTratamento';

export function VeiculosImport() {
    return (
        <ImportacaoWizard
            tipo="VEICULO"
            titulo="Importação de Veículos"
            icone={Truck}
            tomIcone="ambar"
            subtitulo="Envie o CSV de veículos, confira o mapeamento e valide os dados antes de gravar"
            Revisao={Step4RevisaoVeiculos}
            Complemento={ResultadoTratamento}
            identificarLinha={(valores) => ({
                titulo: `Placa ${valores.Placa?.trim() || 'não informada'}`,
                detalhe: [valores.Marca, valores.Modelo].filter((v) => v?.trim()).join(' ') || 'Modelo não informado',
            })}
            rotuloGravadas="veículos gravados"
            confirmacao={(v) =>
                `${v.linhasValidas} veículos serão gravados a partir de ${v.arquivoNome}: placas novas são criadas e as que já existem são atualizadas. Até aqui nada foi gravado. Esta é a etapa que grava.`
            }
        />
    );
}
