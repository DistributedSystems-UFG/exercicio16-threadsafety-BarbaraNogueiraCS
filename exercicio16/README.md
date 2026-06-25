# Exercício 16 - Thread safety com imutabilidade

Este projeto contém programas clientes para testar duas versões de uma classe RGB:

- `SynchronizedRGB`: classe mutável que usa sincronização.
- `ImmutableRGB`: classe imutável que não precisa de sincronização nos métodos.

## Compilação

```bash
mkdir -p out
javac -d out src/*.java
```

## Execução

```bash
java -cp out SynchronizedRGBClient
java -cp out ImmutableRGBClient
```

## Resultado esperado

No teste de `SynchronizedRGB`, leituras compostas feitas sem sincronização externa podem apresentar inconsistências entre `getRGB()` e `getName()`.
Isso não significa que os métodos individuais estejam desprotegidos; significa que a sequência `getRGB()` + `getName()` não é atômica do ponto de vista do cliente.

No teste de `ImmutableRGB`, o objeto original não deve mudar, e os contadores de erro devem ficar em zero.
