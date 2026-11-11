import java.util.Scanner;
import java.util.Random;

public class CidadeChuva {
    static void main() {

        //Entrada dos integrantes e cálculo do RM
        Scanner sc = new Scanner(System.in);

        System.out.println("Informe a quantidade de integrantes: ");
        int quantidade = sc.nextInt();

        while (quantidade < 1 || quantidade > 3) {
            System.out.println("Quantidade de integrantes inválida.\nInforme a quantidade novamente: ");
            quantidade = sc.nextInt();
        }

        int[] rms = new int[quantidade];

        for (int i = 0; i < quantidade; i++) {
            System.out.println("Informe o RM do integrante: ");
            rms[i] = sc.nextInt();
        }

        int rmEquipe = calcularRmEquipe(rms);



        //Cálculo dos parâmetros
        int[] parametros = calcularParametros(rmEquipe);
        int n = parametros[0];
        int m = parametros[1];
        int k = parametros[2];
        int l = parametros[3];
        int ordem = parametros[4];



        //Geração e preparação das matrizes
        Random gerador = new Random(rmEquipe);

        int[][] altitude = gerarAltitude(gerador, n, m);
        int[][] lamina = gerarMatriz(gerador, n, m, 0, 10);
        int[][] laminaInicial = copiar(lamina);
        int[][] laminaD1 = copiar(laminaInicial);
        int[][] laminaD2 = copiar(laminaInicial);
        int[] deslocamentoLinha = criarDeslocamentoLinha(ordem);
        int[] deslocamentoColuna = criarDeslocamentoColuna(ordem);



        //Impressão das matrizes iniciais
        imprimir(altitude, "\nAltitude (cm)");
        imprimir(lamina, "\nLâmina (cm)");



        //Simulação dos ciclos e alertas
        boolean[][] alertasJaEmitidos = new boolean[lamina.length][lamina[0].length];

        for (int ciclo = 1; ciclo <= k; ciclo++) {
            int totalAntes = somar(lamina);

            lamina = simularPasso(altitude, lamina, deslocamentoLinha, deslocamentoColuna);

            if (ciclo == 1) {
                imprimir(lamina, "\nPrimeiro Ciclo");
            }

            System.out.println("\n========== CICLO " + (ciclo) + " ==========");
            System.out.println("Total antes: " + totalAntes);

            boolean[][] alertaCiclo = marcarAlerta(lamina, l);

            for (int i = 0; i < alertaCiclo.length; i++) {
                for (int j = 0; j < alertaCiclo[i].length; j++) {
                    if (alertaCiclo[i][j] == true && alertasJaEmitidos[i][j] == false) {
                        alertasJaEmitidos[i][j] = true;
                        System.out.println("ALERTA! Ciclo " + (ciclo) + " - Célula " + "[" + i + "]" + "[" + j + "]" + " atingiu o limite.");
                    }
                }
            }

            int totalDepois = somar(lamina);
            System.out.println("Total Depois: " + totalDepois);

            //DUVIDA D1, D2
            if (ciclo == 2) {
                laminaD1 = copiar(lamina);
            }
            if (ciclo == k) {
                imprimir(lamina, "\nÚltimo Ciclo");
            }
        }



        //Análise final dos alertas
        boolean[][] alerta = marcarAlerta(lamina, l);
        int numeroAlerta = contarAlerta(alerta);

        imprimirMapaAlerta(alerta, "\nMapa de alerta (# = alagado, . = normal):");
        System.out.println("Quantidade de células em alerta: " + numeroAlerta);



        //Cálculo da água por linha
        int[] aguaPorLinha = calcularAguaPorLinha(lamina);
        int linhaCritica = encontrarLinhaCritica(aguaPorLinha);



        //Simulação ingênua e D1/D2
        int[][] laminaIngenua = copiar(laminaInicial);


        for (int ciclo = 1; ciclo <= 3; ciclo++) {
            laminaIngenua = simularPassoIngenuo(altitude, laminaIngenua, deslocamentoLinha, deslocamentoColuna);

            //DUVIDA D1, D2
            if (ciclo == 2) {
                laminaD2 = copiar(laminaIngenua);
            }
        }

        int linhaD1D2 = rmEquipe % n;
        int colunaD1D2 = (rmEquipe / 7) % m;
        int d1 = laminaD1[linhaD1D2][colunaD1D2];
        int d2 = laminaD2[linhaD1D2][colunaD1D2];



        //Impressão dos resultados finais
        System.out.println("\nÁgua por linha (cm): ");
        for (int i = 0; i < aguaPorLinha.length; i++) {
            System.out.println("Linha " + i + ": " + aguaPorLinha[i]);
        }
        System.out.println("Linha crítica: " + linhaCritica + " (" + aguaPorLinha[linhaCritica] + " cm)");


        System.out.println("\nRESUMO RM=" + rmEquipe +
                "\nALERTA=" + numeroAlerta +
                "\nCRITICA=" + linhaCritica + " AGUA=" + aguaPorLinha[linhaCritica] +
                "\nD1=" + d1 + " D2=" + d2);
    }

    //METODO RESPONSAVEL POR RECEBER O RM DOS INTEGRANTES DA EQUIPE, REALIZAR O CALCULO COM O FOR PERCORRENDO TODOS OS INDICES E RETORNAR A SOMA DE TODOS OS RMS PARA REALIZAR O CALCULO DOS PARAMETROS.
    static int calcularRmEquipe(int[] rms) {
        int soma = 0;
        for (int i = 0; i < rms.length; i++) {
            soma += rms[i];
        }
        return soma;
    }

    //METODO RESPONSAVEL PELO CALCULO DOS PARAMETROS COM BASE NO RM DA EQUIPE, RETORNA EM UM VETOR COM OS INDICES DE 0 A 4 OS PARAMETROS n, m, k, l E ordem. NECESSARIO PARA DETERMINAR TAMANHO DAS MATRIZES, QUANTIDADE DE CICLOS, LIMIAR DE ALERTA E ORDEM DOS VIZINHOS.
    static int[] calcularParametros(int rm) {
        int[] parametros = new int[5];
        parametros[0] = 8 + rm % 3;
        parametros[1] = 10 + (rm / 10) % 3;
        parametros[2] = 4 + (rm / 100) % 3;
        parametros[3] = 10 + 2 * (rm % 4);
        parametros[4] = rm % 2;
        return parametros;
    }

    //METODO RESPONSAVEL POR GERAR A MATRIZ DE ALTITUDE, RECEBE O ARGUMENTO gerador ALEM DOS PARAMETROS n E m, APLICA A FORMULA FORNECIDA E RETORNA A MATRIZ altitude PREENCHIDA.
    static int[][] gerarAltitude(Random gerador, int n, int m) {
        int[][] altitude = new int[n][m];
        for (int i = 0; i < n; i++){
            for (int j = 0; j < m; j++){
                altitude[i][j] = 3 * Math.abs(i - n / 2) + gerador.nextInt(5);
            }
        }
        return altitude;
    }

    //METODO RESPONSAVEL POR GERAR UMA MATRIZ ALEATORIA, RECEBE OS PARAMETROS n E m TAMBEM RECEBE OS ARGUMENTOS gerador, minimo e maximo QUE DEFINEM O INTERVALO DOS VALORES ALEATORIOS. APLICA A FORMULA FORNECIDA E RETORNA A MATRIZ PREENCHIDA.
    static int[][] gerarMatriz(Random gerador, int n, int m, int minimo, int maximo) {
        int[][] matriz = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matriz[i][j] = minimo + gerador.nextInt(maximo - minimo + 1);
            }
        }
        return matriz;
    }

    //METODO RESPONSAVEL POR REALIZAR A IMPRESSÃO DAS MATRIZES, RECEBE DOIS ARGUMENTOS DIFERENTES EM CADA CHAMADA, A CHAMADA ENVIA A MATRIZ E SEU RESPECTIVO TITULO.
    static void imprimir(int[][] matriz, String titulo) {
        System.out.println(titulo);
        for (int i = 0; i < matriz.length; i++) {
            String linha = "";
            for (int j = 0; j < matriz[i].length; j++) {
                linha += matriz[i][j] + "\t";
            }
            System.out.println(linha);
        }
    }

    //METODO RESPONSAVEL POR SOMAR TODOS OS ELEMENTOS DE UMA MATRIZ, PERCORRE TODAS AS LINHAS E COLUNAS, E RETORNA O RESULTADO DA SOMA. UTILIZADO PARA VERIFICAR O TOTAL DE AGUA DA LAMINA, ANTES E DEPOIS DE CADA CICLO.
    static int somar(int[][] matriz) {
        int soma = 0;
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                soma += matriz[i][j];
            }
        }
        return soma;
    }

    //METODO
    static int[][] copiar(int[][] matriz) {
        int[][] copia = new int[matriz.length][matriz[0].length];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++){
                copia[i][j] = matriz[i][j];
            }
        }
        return copia;
    }

    //METODO
    static boolean dentro(int i, int j, int n, int m) {
        if (i >= 0 && i < n && j >= 0 && j < m) {
            return true;
        } else {
            return false;
        }
    }

    //METODO
    static int[] criarDeslocamentoLinha(int ordem) {
        int[] deslocamentoLinha = new int[4];
        if (ordem == 0) {
            deslocamentoLinha[0] = -1;
            deslocamentoLinha[1] = 0;
            deslocamentoLinha[2] = 1;
            deslocamentoLinha[3] = 0;
        } else {
            deslocamentoLinha[0] = 0;
            deslocamentoLinha[1] = 1;
            deslocamentoLinha[2] = 0;
            deslocamentoLinha[3] = -1;
        }
        return deslocamentoLinha;
    }

    //METODO
    static int[] criarDeslocamentoColuna(int ordem) {
        int[] deslocamentoColuna = new int[4];
        if (ordem == 0) {
            deslocamentoColuna[0] = 0;
            deslocamentoColuna[1] = 1;
            deslocamentoColuna[2] = 0;
            deslocamentoColuna[3] = -1;
        } else {
            deslocamentoColuna[0] = -1;
            deslocamentoColuna[1] = 0;
            deslocamentoColuna[2] = 1;
            deslocamentoColuna[3] = 0;
        }
        return deslocamentoColuna;
    }

    //METODO
    static int[][] simularPasso(
            int[][] altitude,
            int[][] lamina,
            int[] deslocamentoLinha,
            int[] deslocamentoColuna) {
        int[][] nova = copiar(lamina);
        for (int i = 0; i < lamina.length; i++) {
            for (int j = 0; j < lamina[i].length; j++) {
                int t = lamina[i][j] / 2;
                if (t > 0) {
                    int nivelAtual = altitude [i][j] + lamina[i][j];
                    int menorNivel = nivelAtual;
                    int melhorDirecao = -1;
                    for (int d = 0; d < 4 ; d++) {
                        int vizinhoLinha = i + deslocamentoLinha[d];
                        int vizinhoColuna = j + deslocamentoColuna[d];
                        if (dentro(vizinhoLinha, vizinhoColuna, lamina.length, lamina[0].length )) {
                            int nivelVizinho = altitude[vizinhoLinha][vizinhoColuna] + lamina[vizinhoLinha][vizinhoColuna];
                            if (nivelVizinho < menorNivel) {
                                menorNivel = nivelVizinho;
                                melhorDirecao = d;
                            }
                        }
                    }
                    if (melhorDirecao != -1) {
                        int vizinhoLinha = i + deslocamentoLinha[melhorDirecao];
                        int vizinhoColuna = j + deslocamentoColuna[melhorDirecao];
                        nova[i][j] -= t;
                        nova[vizinhoLinha][vizinhoColuna] += t;
                    }
                }
            }
        }
        return nova;
    }

    //METODO
    static boolean[][] marcarAlerta(int[][] lamina, int limiarAlerta) {
        boolean[][] alerta = new boolean[lamina.length][lamina[0].length];
        for (int i = 0; i < lamina.length; i++) {
            for (int j = 0; j < lamina[i].length; j++) {
                if (lamina[i][j] >= limiarAlerta) {
                    alerta[i][j] = true;
                } else {
                    alerta[i][j] = false;
                }
            }
        }
        return alerta;
    }

    //METODO
    static int contarAlerta(boolean[][] alerta){
        int contador = 0;
        for (int i = 0; i < alerta.length; i++) {
            for (int j = 0; j < alerta[i].length; j++) {
                if (alerta[i][j] == true) {
                    contador++;
                }
            }
        }
        return contador;
    }

    //METODO
    static void imprimirMapaAlerta(boolean[][] alerta, String titulo) {
        System.out.println(titulo);
        for (int i = 0; i < alerta.length; i++) {
            String linha = "";
            for (int j = 0; j < alerta[i].length; j++) {
                if (alerta[i][j] == true) {
                    linha += "#" + "\t";
                } else {
                    linha += "." + "\t";
                }
            }
            System.out.println(linha);
        }
    }

    //METODO
    static int[] calcularAguaPorLinha(int[][] lamina) {
        int[] aguaPorLinha = new int[lamina.length];
        for (int i = 0; i < lamina.length; i++) {
            int acumuloAgua = 0;
            for (int j = 0; j < lamina[i].length; j++) {
                acumuloAgua += lamina[i][j];
            }
            aguaPorLinha[i] += acumuloAgua;
        }
        return aguaPorLinha;
    }

    //METODO
    static int encontrarLinhaCritica(int[] aguaPorLinha) {
        int linhaCritica = 0;
        int maiorAgua = aguaPorLinha[0];
        for (int i = 1; i < aguaPorLinha.length; i++) {
            if (aguaPorLinha[i] > maiorAgua) {
                maiorAgua = aguaPorLinha[i];
                linhaCritica = i;
            }
        }
        return linhaCritica;
    }

    //METODO
    static int[][] simularPassoIngenuo(
            int[][] altitude,
            int[][] lamina,
            int[] deslocamentoLinha,
            int[] deslocamentoColuna) {
        for (int i = 0; i < lamina.length; i++) {
            for (int j = 0; j < lamina[i].length; j++) {
                int t = lamina[i][j] / 2;
                if (t > 0) {
                    int nivelAtual = altitude [i][j] + lamina[i][j];
                    int menorNivel = nivelAtual;
                    int melhorDirecao = -1;
                    for (int d = 0; d < 4 ; d++) {
                        int vizinhoLinha = i + deslocamentoLinha[d];
                        int vizinhoColuna = j + deslocamentoColuna[d];
                        if (dentro(vizinhoLinha, vizinhoColuna, lamina.length, lamina[0].length )) {
                            int nivelVizinho = altitude[vizinhoLinha][vizinhoColuna] + lamina[vizinhoLinha][vizinhoColuna];
                            if (nivelVizinho < menorNivel) {
                                menorNivel = nivelVizinho;
                                melhorDirecao = d;
                            }
                        }
                    }
                    if (melhorDirecao != -1) {
                        int vizinhoLinha = i + deslocamentoLinha[melhorDirecao];
                        int vizinhoColuna = j + deslocamentoColuna[melhorDirecao];
                        lamina[i][j] -= t;
                        lamina[vizinhoLinha][vizinhoColuna] += t;
                    }
                }
            }
        }
        return lamina;
    }
}