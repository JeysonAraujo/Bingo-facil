
# 01
olá precisamos gerar uma plicação web com o contexto de bingo online. 
Gerando cartelas automáticas com um atela de visualização com sorteiro ao vivo online,
o croqui para esta tale seria o video(Transmissão) acima e abaixo as cartelas selecionadas pelo usuário, ao decorrer o sorteio as cartelas iram marcar automaticamente os numeros sorteados, que seram inseridos manualmente pelo Administrador do sistema.

Sera necesario inserir os numeros sorteados no sistema, para que as cartelas sejam marcadas automaticamente.



# 02 Aprimoramentos sugeridos

O padrão de de premiação será determinado pelo administrador do sistema, que cria o evento do bingo (bingo dia das mães) e define quais rodadas, padrão de marcação (Ex: Linha, Coluna, Bingo Cheio, etc.), premios, e quantidade de acertos para cada prêmio.

O escopo do usuário seria algo mais regional/local, ou seja o usuário compra a cartela de um vendedor, que por sua vez esta cadastrado no sistema, 
para participar do sorteio online, o usuário insere o código da cartela no sistema e valida, adiciona seu nome e telefone e é direcionado para a tela de jogo onde ele podera ver a transmissão ao vivo e as cartelas selecionadas pelo usuário, conforme o sorteio for acontecendo, as cartelas selecionadas pelo usuário iram marcar os números sorteados automaticamente.

Nesse meio tempo o usuário poderá interagir com outros usuários, mas de forma limitada, apenas enviando mensagens de texto, podendo 
interagir também com o administrador do sistema, que poderá enviar mensagens de texto para os usuários,.
Ao acompanhar o sorteio o usuário poderá "gritar" Bingo através de um botão, que será enviado para o administrador do sistema,
que por sua vez irá verificar se o usuário tem direito ao prêmio, caso tenha, o administrador do sistema irá parabenizar o usuário,
caso o usuário não tenha direito ao prêmio, o administrador do sistema irá informar o motivo.



# 03 
Sobre os vendedores, eles não precisaram de um painel específico, o admim gera um arquivo PDF com as cartelas para a impressão, nela conteria um código único para cada cartela e um qr code com o id, que será usado pelo usuário para validar sua participação no sorteio.

A gestão de pagamentos é feita pelo admin em um sistema a parte, nesse sistema de bingo só vai ter o cadastro do usuario, a compra da cartela e o sorteio.



# 04
A aplicação será em Vue.js e o backend será em Java com Spring Boot.


# 05
Ah um adendo sobre as tabelas, seria um cartelão, que conteria 4 cartelas dentro de uma só, cada cartela conteria 25 numeros, com um espaço livre no centro de cada cartela. 

-------

A solução da sincronia de latência seria usar o WebSocket para enviar os números sorteados para o front-end, garantindo que a marcação seja feita em tempo real.

Sobre a fila de gritos, poderiamos, fazer com que ao identificar um bingo, mostrasse ao admin o jogador que fez o bingo e mostrasse uma animação na tela para o jogador, parabenizando-o pelo bingo.

Sobre a segurança, garantir que o usuário não possa manipular o sistema para ganhar o prêmio. Mas pode seguir da melhor forma para atingir esse objetivo.



1. A classificação está de acordo com o esperado.
2. Sobre o foco regional inicialmente pretendemos mante=lo regionalmente mas futuramente pretendemos expandir para outras regiões e fechar algumas parcerias.

Na verdade o nome está errado, seria bingo-ai se puder corrigir isso no briefing, ficaria ótimo. E prosseguimos para as perguntas.


1. Penso que seria interessante colocar uma animação Parabenizando o jogador pelo bingo, com um som de vitória/sino, com um efeito visual de confetes. Uma outra animação que poderiámos incluir seria quando o usuário estevesse perto de fazer um bingo mostrace algo como "Quase lá!" ou "Olha a boa!". 

2. Sim exatamente como descreveu. 

3. Existe uma demanda que quero oferecer para uma igreja local, que realiza vários bingos durante o ano. Além de ser possivel negociar com outras pessoas que trabalham com eventos ou queiram relizar um bingo mais acessivel e moderno. 

# Reagras

1. Apenas registrar um log para auditoria.

2. A questão d asegurnaça quanto mais seguro melhor, sobre a perda ou roubo de uma cartela física o admin teria que registrar a cartela como perdida e gerar uma nova para o usuário.

3. Em questão ao delay pode ser um delay proposital ou o que julgar melhor para atingir o objetivo.

4. Se houver algum erro em alguma tabela ele deve ser reportado de preferência antes do sorteio para ser corrigido, mas o admin teria que ter a opção de editar as cartelas e prêmios, para caso precise corrigir algo durante o sorteio.



# UX

1. Os jogadores não estaram no evento presencialmente, eles apenas acompanhariam a transmissão do bingo por um celular ou computador, poderam ver sua tabela digitalmente sendo marcada automáticamente ou se preferirem ocultar a visualização para acompanhar pela cartela física impressa. 

2. Acredito que uma dor não só para idosos mas para outras pessoas leigas é a questão do sistema não possuir um guia antes de usar. Como o foco não é apenas idosos um guia de uso mostrando passo a passo de como participar do sorteio seria interessante.

3. A aplicação não seria focada apenas em um dispositivo, deve ser resposiva para adaptar em diferentes formatos de tela. Como computadores, tablets e smartphones.
