import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class TestePraAtividade {

  private static final int N = 10000;
  private static final int M = 1000;
  private static final long SEED = 42L;

  public static void main(String[] args) {
    List<Integer> valoresAleatorios = gerarValoresAleatorios(N, SEED);
    List<Integer> buscas = gerarValoresAleatorios(M, SEED + 1);

    Resultado resultadoAbbAleatorio = executarExperimento(valoresAleatorios, buscas, false);
    Resultado resultadoAvlAleatorio = executarExperimento(valoresAleatorios, buscas, true);

    List<Integer> valoresOrdenados = new ArrayList<>(valoresAleatorios);
    valoresOrdenados.sort(null);

    Resultado resultadoAbbOrdenado = executarExperimento(valoresOrdenados, buscas, false);
    Resultado resultadoAvlOrdenado = executarExperimento(valoresOrdenados, buscas, true);

    imprimirResultados(resultadoAbbAleatorio, resultadoAvlAleatorio, resultadoAbbOrdenado, resultadoAvlOrdenado);
  }

  private static List<Integer> gerarValoresAleatorios(int quantidade, long semente) {
    List<Integer> valores = new ArrayList<>(quantidade);
    Random random = new Random(semente);
    for (int i = 0; i < quantidade; i++) {
      valores.add(random.nextInt());
    }
    return valores;
  }

  private static Resultado executarExperimento(List<Integer> valoresInsercao, List<Integer> valoresBusca, boolean avl) {
    ABB<Integer, Integer> arvore = avl ? new AVL<>() : new ABB<>();

    for (Integer valor : valoresInsercao) {
      arvore.inserir(valor, valor);
    }

    long comparacoesTotais = 0;
    double tempoTotal = 0;

    for (Integer valor : valoresBusca) {
      try {
        arvore.pesquisar(valor);
      } catch (NoSuchElementException e) {
        // valor não está na árvore; contabilizamos comparações e tempo mesmo assim
      }
      comparacoesTotais += arvore.getComparacoes();
      tempoTotal += arvore.getTempo();
    }

    return new Resultado(comparacoesTotais, tempoTotal);
  }

  private static void imprimirResultados(Resultado abbAleatorio, Resultado avlAleatorio, Resultado abbOrdenado,
      Resultado avlOrdenado) {
    System.out.printf("%-28s %16s %16s%n", "Experimento", "Comparações", "Tempo (ms)");
    System.out.println("-------------------------------------------------------------");
    System.out.printf("%-28s %16d %16.2f%n", "ABB / inserção aleatória", abbAleatorio.comparacoes, abbAleatorio.tempo);
    System.out.printf("%-28s %16d %16.2f%n", "AVL / inserção aleatória", avlAleatorio.comparacoes, avlAleatorio.tempo);
    System.out.printf("%-28s %16d %16.2f%n", "ABB / inserção ordenada", abbOrdenado.comparacoes, abbOrdenado.tempo);
    System.out.printf("%-28s %16d %16.2f%n", "AVL / inserção ordenada", avlOrdenado.comparacoes, avlOrdenado.tempo);
  }

  private static class Resultado {
    private final long comparacoes;
    private final double tempo;

    Resultado(long comparacoes, double tempo) {
      this.comparacoes = comparacoes;
      this.tempo = tempo;
    }
  }
}
