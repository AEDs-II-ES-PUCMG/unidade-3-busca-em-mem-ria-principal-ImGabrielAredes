import java.util.Comparator;

public class AVL<K, V> extends ABB<K, V> {

  /**
   * Construtor da classe.
   * Esse construtor cria uma nova árvore binária de busca AVL vazia.
   */
  public AVL() {
    super();
  }

  public AVL(Comparator<K> comparador) {
    super(comparador);
  }

  /**
   * Método recursivo responsável por adicionar um item à árvore.
   * 
   * @param raizArvore: raiz da árvore ou sub-árvore em que o item será
   *                    adicionado.
   * @param item:       item que deverá ser adicionado à árvore.
   * @return a raiz atualizada da árvore ou sub-árvore balanceada AVL em que o
   *         item foi adicionado.
   */
  @Override
  protected No<K, V> inserir(No<K, V> raizArvore, K chave, V item) {

    return balancear(super.inserir(raizArvore, chave, item));
  }

  /**
   * Método recursivo responsável por localizar na árvore ou sub-árvore o
   * antecessor do nó que deverá ser retirado.
   * O antecessor do nó que deverá ser retirado da árvore corresponde
   * ao nó que armazena o item que é o maior,
   * dentre os itens menores do que o item que deverá ser retirado.
   * Depois de ser localizado na árvore ou sub-árvore,
   * o antecessor do nó que deverá ser retirado da árvore o substitui.
   * Adicionalmente, a árvore ou sub-árvore balanceada AVL é atualizada com a
   * remoção do antecessor.
   * 
   * @param itemRetirar: referência ao nó que armazena o item que deverá ser
   *                     retirado da árvore.
   * @param raizArvore:  raiz da árvore ou sub-árvore em que o antecessor do nó
   *                     que deverá ser retirado deverá ser localizado.
   * @return a raiz atualizada da árvore ou sub-árvore balanceada AVL após a
   *         remoção do antecessor do nó que foi retirado da árvore.
   */
  @Override
  protected No<K, V> removerNoAntecessor(No<K, V> itemRetirar, No<K, V> raizArvore) {

    return balancear(super.removerNoAntecessor(itemRetirar, raizArvore));
  }

  /**
   * Método recursivo responsável por localizar um item na árvore e retirá-lo da
   * árvore.
   * 
   * @param raizArvore:  raiz da árvore ou sub-árvore da qual o item será
   *                     retirado.
   * @param itemRemover: item que deverá ser localizado e removido da árvore.
   * @return a raiz atualizada da árvore ou sub-árvore balanceada AVL da qual o
   *         item foi retirado.
   */
  @Override
  protected No<K, V> remover(No<K, V> raizArvore, K chaveRemover) {

    return balancear(super.remover(raizArvore, chaveRemover));
  }

  /**
   * Método responsável por verificar se a raiz da árvore ou sub-árvore recebida
   * está desbalanceada
   * e, caso esteja, restaurar o balanceamento AVL por meio de rotações simples ou
   * duplas.
   *
   * Dicas:
   * - O fator de balanceamento de um nó é obtido por meio do método
   * "getFatorBalanceamento()" da classe No.
   * - Fator de balanceamento igual a 2 indica que a árvore está desbalanceada à
   * esquerda
   * (a sub-árvore esquerda é mais alta do que deveria);
   * - Fator de balanceamento igual a -2 indica que a árvore está desbalanceada à
   * direita
   * (a sub-árvore direita é mais alta do que deveria);
   * - Quando o desbalanceamento ocorre "para um lado" e o filho responsável pelo
   * desbalanceamento
   * está pendendo para o lado oposto, é necessário realizar uma rotação dupla
   * (uma rotação no filho, seguida de uma rotação na raiz).
   * - Quando a árvore não está desbalanceada (fator de balanceamento entre -1 e
   * 1),
   * apenas a altura do nó precisa ser recalculada (utilize o método "setAltura()"
   * do nó).
   *
   * @param raizArvore: raiz da árvore ou sub-árvore que deverá ser balanceada.
   * @return a raiz atualizada da árvore ou sub-árvore após o balanceamento AVL.
   */
  private No<K, V> balancear(No<K, V> raizArvore) {

    int fatorBalanceamento;
    int fatorBalanceamentoFilho;

    if (raizArvore != null) {

      fatorBalanceamento = raizArvore.getFatorBalanceamento();

      if (fatorBalanceamento == 2) {
        No<K, V> filhoEsquerda = raizArvore.getEsquerda();
        if (filhoEsquerda != null && filhoEsquerda.getFatorBalanceamento() < 0) {
          raizArvore.setEsquerda(rotacionarEsquerda(filhoEsquerda));
        }
        raizArvore = rotacionarDireita(raizArvore);
      } else if (fatorBalanceamento == -2) {
        No<K, V> filhoDireita = raizArvore.getDireita();
        if (filhoDireita != null && filhoDireita.getFatorBalanceamento() > 0) {
          raizArvore.setDireita(rotacionarDireita(filhoDireita));
        }
        raizArvore = rotacionarEsquerda(raizArvore);
      } else
        raizArvore.setAltura();
    }
    return raizArvore;
  }

  /**
   * Método responsável por realizar uma rotação simples à direita em torno do nó
   * recebido.
   *
   * Esquema da rotação (p é o nó recebido, u é o filho à esquerda de p):
   *
   * p u
   * / \ / \
   * u ? -----> ? p
   * / \ / \
   * ? X X ?
   *
   * Onde "X" (filhoEsquerdaDireita) é o filho à direita de "u", que passa a ser
   * o filho à esquerda de "p" após a rotação.
   *
   * Após reorganizar as referências dos nós envolvidos, lembre-se de atualizar
   * a altura de cada nó que teve sua sub-árvore modificada (utilizando
   * "setAltura()"),
   * sempre atualizando primeiro o nó que ficou mais abaixo na árvore.
   *
   * @param p: nó em torno do qual a rotação simples à direita deverá ser
   *           realizada.
   * @return a nova raiz da sub-árvore após a rotação (o nó "u").
   */
  private No<K, V> rotacionarDireita(No<K, V> p) {

    if (p == null || p.getEsquerda() == null) {
      return p;
    }

    No<K, V> u = p.getEsquerda();
    No<K, V> filhoEsquerdaDireita = u.getDireita();

    p.setEsquerda(filhoEsquerdaDireita);
    u.setDireita(p);

    p.setAltura();
    u.setAltura();
    return u;
  }

  /**
   * Método responsável por realizar uma rotação simples à esquerda em torno do nó
   * recebido.
   *
   * Esquema da rotação (p é o nó recebido, z é o filho à direita de p):
   *
   * p z
   * / \ / \
   * ? z -----> p ?
   * / \ / \
   * X ? ? X
   *
   * Onde "X" (filhoDireitaEsquerda) é o filho à esquerda de "z", que passa a ser
   * o filho à direita de "p" após a rotação.
   *
   * Após reorganizar as referências dos nós envolvidos, lembre-se de atualizar
   * a altura de cada nó que teve sua sub-árvore modificada (utilizando
   * "setAltura()"),
   * sempre atualizando primeiro o nó que ficou mais abaixo na árvore.
   *
   * @param p: nó em torno do qual a rotação simples à esquerda deverá ser
   *           realizada.
   * @return a nova raiz da sub-árvore após a rotação (o nó "z").
   */
  private No<K, V> rotacionarEsquerda(No<K, V> p) {

    if (p == null || p.getDireita() == null) {
      return p;
    }

    No<K, V> z = p.getDireita();
    No<K, V> filhoDireitaEsquerda = z.getEsquerda();

    p.setDireita(filhoDireitaEsquerda);
    z.setEsquerda(p);

    p.setAltura();
    z.setAltura();
    return z;
  }
}
