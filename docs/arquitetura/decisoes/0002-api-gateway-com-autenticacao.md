# ADR 0002: API Gateway como entrada única, validando o JWT

**Status:** aceita · **Data:** 20/09/2026

## Contexto

Com três serviços, o frontend precisaria conhecer três portas, e cada serviço repetiria a configuração de CORS e a validação do token. Além disso, o `ms-operacoes` decidia o perfil pelo header `X-User-Role` enviado pelo próprio cliente: bastava um `curl` com `X-User-Role: GESTOR` para acessar dados financeiros.

## Decisão

Criar o **api-gateway** (Spring Cloud Gateway WebMVC, porta 8080) como único endereço público da API. Ele:

1. roteia por prefixo de caminho para o serviço dono;
2. valida o JWT (assinatura, emissor `ms-usuarios` e validade);
3. **descarta** qualquer `X-User-Name` ou `X-User-Role` vindo do cliente e injeta os valores tirados do token;
4. concentra a configuração de CORS.

## Consequências

- O frontend conhece uma única URL (`VITE_API_URL`).
- Os serviços podem confiar nos headers de identidade, **desde que não sejam expostos diretamente para fora**.
- O gateway e o ms-usuarios precisam compartilhar a mesma chave (`API_SECURITY_TOKEN_SECRET`).
- Sem a chave configurada, o gateway não sobe. É proposital: é melhor não subir do que aceitar token sem verificar.
- O gateway vira ponto único de falha, o que é aceitável no escopo do projeto.
