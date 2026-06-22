import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ABB<K, V> implements IMapeamento<K, V> {

	private No<K, V> raiz; // referência à raiz da árvore.
	private Comparator<K> comparador; // comparador empregado para definir "menores" e "maiores".
	private int tamanho;
	private long comparacoes;
	private long inicio;
	private long termino;

	/**
	 * Método auxiliar para inicialização da árvore binária de busca.
	 * 
	 * Este método define a raiz da árvore como {@code null} e seu tamanho como 0.
	 * Utiliza o comparador fornecido para definir a organização dos elementos na
	 * árvore.
	 * 
	 * @param comparador o comparador para organizar os elementos da árvore.
	 */
	private void init(Comparator<K> comparador) {
		raiz = null;
		tamanho = 0;
		this.comparador = comparador;
	}

	/**
	 * Construtor da classe.
	 * O comparador padrão de ordem natural será utilizado.
	 */
	@SuppressWarnings("unchecked")
	public ABB() {
		init((Comparator<K>) Comparator.naturalOrder());
	}

	/**
	 * Construtor da classe.
	 * Esse construtor cria uma nova árvore binária de busca vazia.
	 * 
	 * @param comparador o comparador a ser utilizado para organizar os elementos da
	 *                   árvore.
	 */
	public ABB(Comparator<K> comparador) {
		init(comparador);
	}

	/**
	 * Construtor da classe.
	 * Esse construtor cria uma nova árvore binária de busca a partir de uma outra
	 * árvore binária de busca,
	 * com os mesmos itens, mas usando uma nova chave.
	 * 
	 * @param original    a árvore binária de busca original.
	 * @param funcaoChave a função que irá extrair a nova chave de cada item para a
	 *                    nova árvore.
	 */
	@SuppressWarnings("unchecked")
	public ABB(ABB<?, V> original, Function<V, K> funcaoChave) {
		ABB<K, V> nova = new ABB<>();
		nova = copiarArvore(original.raiz, funcaoChave, nova);
		this.raiz = nova.raiz;
		this.comparador = (Comparator<K>) Comparator.naturalOrder();
	}

	/**
	 * Recursivamente, copia os elementos da árvore original para esta, num processo
	 * análogo ao caminhamento em ordem.
	 * 
	 * @param <T>         Tipo da nova chave.
	 * @param raizArvore  raiz da árvore original que será copiada.
	 * @param funcaoChave função extratora da nova chave para cada item da árvore.
	 * @param novaArvore  Nova árvore. Parâmetro usado para permitir o retorno da
	 *                    recursividade.
	 * @return A nova árvore com os itens copiados e usando a chave indicada pela
	 *         função extratora.
	 */
	private <T> ABB<T, V> copiarArvore(No<?, V> raizArvore, Function<V, T> funcaoChave, ABB<T, V> novaArvore) {

		if (raizArvore != null) {
			novaArvore = copiarArvore(raizArvore.getEsquerda(), funcaoChave, novaArvore);
			V item = raizArvore.getItem();
			T chave = funcaoChave.apply(item);
			novaArvore.inserir(chave, item);
			novaArvore = copiarArvore(raizArvore.getDireita(), funcaoChave, novaArvore);
		}
		return novaArvore;
	}

	/**
	 * Método booleano que indica se a árvore está vazia ou não.
	 * 
	 * @return
	 *         verdadeiro: se a raiz da árvore for null, o que significa que a
	 *         árvore está vazia.
	 *         falso: se a raiz da árvore não for null, o que significa que a árvore
	 *         não está vazia.
	 */
	public Boolean vazia() {
		return (this.raiz == null);
	}

	protected No<K, V> inserir(No<K, V> raizArvore, K chave, V item) {
		if (raizArvore == null) {
			tamanho++;
			return new No<>(chave, item);
		}

		int comparacao = comparador.compare(chave, raizArvore.getChave());
		if (comparacao < 0)
			raizArvore.setEsquerda(inserir(raizArvore.getEsquerda(), chave, item));
		else if (comparacao > 0)
			raizArvore.setDireita(inserir(raizArvore.getDireita(), chave, item));
		else
			raizArvore.setItem(item);

		return raizArvore;
	}

	protected No<K, V> removerNoAntecessor(No<K, V> itemRetirar, No<K, V> raizArvore) {
		if (raizArvore.getDireita() != null) {
			raizArvore.setDireita(removerNoAntecessor(itemRetirar, raizArvore.getDireita()));
			return raizArvore;
		}

		itemRetirar.setChave(raizArvore.getChave());
		itemRetirar.setItem(raizArvore.getItem());
		tamanho--;
		return raizArvore.getEsquerda();
	}

	protected No<K, V> remover(No<K, V> raizArvore, K chaveRemover) {
		if (raizArvore == null)
			throw new NoSuchElementException("O item não foi localizado na árvore!");

		int comparacao = comparador.compare(chaveRemover, raizArvore.getChave());
		if (comparacao < 0) {
			raizArvore.setEsquerda(remover(raizArvore.getEsquerda(), chaveRemover));
		} else if (comparacao > 0) {
			raizArvore.setDireita(remover(raizArvore.getDireita(), chaveRemover));
		} else {
			if (raizArvore.getEsquerda() == null) {
				tamanho--;
				return raizArvore.getDireita();
			} else if (raizArvore.getDireita() == null) {
				tamanho--;
				return raizArvore.getEsquerda();
			} else {
				raizArvore.setEsquerda(removerNoAntecessor(raizArvore, raizArvore.getEsquerda()));
			}
		}

		return raizArvore;
	}

	@Override
	/**
	 * Método que encapsula a pesquisa recursiva de itens na árvore.
	 * 
	 * @param chave a chave do item que será pesquisado na árvore.
	 * @return o valor associado à chave.
	 */
	public V pesquisar(K chave) {
		if (chave == null)
			throw new IllegalArgumentException("Chave de pesquisa não pode ser null");
		comparacoes = 0;
		inicio = System.nanoTime();
		V procurado = pesquisar(raiz, chave);
		termino = System.nanoTime();
		return procurado;
	}

	private V pesquisar(No<K, V> raizArvore, K procurado) {

		int comparacao;

		comparacoes++;
		if (raizArvore == null)

			throw new NoSuchElementException("O item não foi localizado na árvore!");

		comparacao = comparador.compare(procurado, raizArvore.getChave());

		if (comparacao == 0)
			return raizArvore.getItem();
		else if (comparacao < 0)

			return pesquisar(raizArvore.getEsquerda(), procurado);
		else

			return pesquisar(raizArvore.getDireita(), procurado);
	}

	@Override
	/**
	 * Método que encapsula a adição recursiva de itens à árvore, associando-o à
	 * chave fornecida.
	 * 
	 * @param chave a chave associada ao item que será inserido na árvore.
	 * @param item  o item que será inserido na árvore.
	 * 
	 * @return o tamanho atualizado da árvore após a execução da operação de
	 *         inserção.
	 */
	public int inserir(K chave, V item) {
		if (chave == null)
			throw new IllegalArgumentException("Chave de inserção não pode ser null");
		this.raiz = inserir(this.raiz, chave, item);
		return tamanho;
	}

	@Override
	public String toString() {
		return percorrer();
	}

	@Override
	public String percorrer() {
		if (vazia())
			return "A árvore está vazia!";
		return percorrer(this.raiz);
	}

	private String percorrer(No<K, V> nodo) {
		if (nodo == null)
			return "";
		StringBuilder resultado = new StringBuilder();
		resultado.append(percorrer(nodo.getEsquerda()));
		resultado.append(nodo.getItem()).append("\n");
		resultado.append(percorrer(nodo.getDireita()));
		return resultado.toString();
	}

	@Override
	/**
	 * Método que encapsula a remoção recursiva de um item da árvore.
	 * 
	 * @param chave a chave do item que deverá ser localizado e removido da árvore.
	 * @return o valor associado ao item removido.
	 */
	public V remover(K chave) {
		if (chave == null)
			throw new IllegalArgumentException("Chave de remoção não pode ser null");
		V item = pesquisar(chave);
		this.raiz = remover(this.raiz, chave);
		return item;
	}

	public Lista<V> recortar(K chaveDeOnde, K chaveAteOnde) {
		if (chaveDeOnde == null || chaveAteOnde == null)
			throw new IllegalArgumentException("As chaves de intervalo não podem ser null");
		if (comparador.compare(chaveDeOnde, chaveAteOnde) > 0)
			throw new IllegalArgumentException("Intervalo inválido: chaveDeOnde > chaveAteOnde");

		Lista<V> resultado = new Lista<>();
		recortarRec(this.raiz, chaveDeOnde, chaveAteOnde, resultado);
		return resultado;
	}

	/**
	 * Percorre a árvore em ordem, podando sub-árvores fora do intervalo [start,
	 * end].
	 */
	private void recortarRec(No<K, V> nodo, K start, K end, Lista<V> out) {
		if (nodo == null)
			return;

		int cmpStart = comparador.compare(nodo.getChave(), start);
		if (cmpStart < 0) {
			recortarRec(nodo.getDireita(), start, end, out);
			return;
		}

		int cmpEnd = comparador.compare(nodo.getChave(), end);
		if (cmpEnd > 0) {
			recortarRec(nodo.getEsquerda(), start, end, out);
			return;
		}

		recortarRec(nodo.getEsquerda(), start, end, out);
		out.inserir(nodo.getItem());
		recortarRec(nodo.getDireita(), start, end, out);
	}

	@Override
	public int tamanho() {
		return tamanho;
	}

	@Override
	public long getComparacoes() {
		return comparacoes;
	}

	@Override
	public double getTempo() {
		return (termino - inicio) / 1_000_000;
	}
}