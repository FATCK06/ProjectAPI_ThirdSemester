package br.com.newe.ms_operacoes.config.frota;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Carrega os cortes de utilizacao do arquivo proprio. @PropertySource tem a
 * menor precedencia, entao variavel de ambiente e application.properties
 * continuam sobrescrevendo.
 */
@Configuration
@PropertySource("classpath:situacao-utilizacao.properties")
@EnableConfigurationProperties(CortesUtilizacao.class)
public class SituacaoUtilizacaoConfig {
}
