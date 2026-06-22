import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.lang.reflect.InvocationTargetException;

public class App {

    /**
     * Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto
     */
    static String nomeArquivoDados;

    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;

    static ABB<Integer, Produto> produtosCadastradosPorId;

    static Map<Produto, Lista<Pedido>> pedidosPorProduto = new HashMap<>();

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }

    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {

        T valor;

        System.out.println(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }

    /**
     * Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * 
     * @return Um inteiro com a opção do usuário.
     */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar produto, por nome");
        System.out.println("3 - Procurar produto, por id");
        System.out.println("4 - Remover produto, por nome");
        System.out.println("5 - Remover produto, por id");
        System.out.println("6 - Recortar a lista de produtos, por nome");
        System.out.println("7 - Recortar a lista de produtos, por id");
        System.out.println("8 - Gerar relatório de pedidos de um produto");
        System.out.println("0 - Finalizar");

        Integer opcao;
        do {
            opcao = lerOpcao("Digite sua opção: ", Integer.class);
            if (opcao == null || opcao < 0 || opcao > 8) {
                System.out.println("Opção inválida. Digite um número entre 0 e 8.");
            }
        } while (opcao == null || opcao < 0 || opcao > 8);

        return opcao;
    }

    /**
     * Lê os dados de um arquivo-texto e retorna uma ávore de produtos.
     * Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em
     * caso de problemas com o arquivo.
     * 
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas
     *         de leitura.
     */
    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {

        ABB<K, Produto> produtosCadastrados = new ABB<>();
        int numProdutos;
        String linha;

        try (Scanner arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"))) {
            numProdutos = Integer.parseInt(arquivo.nextLine());

            for (int i = 0; i < numProdutos; i++) {
                if (!arquivo.hasNextLine()) {
                    System.out.printf("Arquivo de produtos incompleto: esperava %d produtos e encontrou %d.%n",
                            numProdutos, i);
                    break;
                }
                linha = arquivo.nextLine();
                try {
                    Produto produto = Produto.criarDoTexto(linha);
                    K chave = extratorDeChave.apply(produto);
                    produtosCadastrados.inserir(chave, produto);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    System.out.printf("Linha %d inválida e foi ignorada: %s%n", i + 2, e.getMessage());
                }
            }
            quantosProdutos = produtosCadastrados.tamanho();
        } catch (IOException e) {
            System.out.println("Erro ao abrir o arquivo de produtos: " + e.getMessage());
        } catch (NumberFormatException | java.util.NoSuchElementException e) {
            System.out.println("Formato inválido no arquivo de produtos: " + e.getMessage());
        }

        return produtosCadastrados;
    }

    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
        if (produtosCadastrados == null || procurado == null) {
            return null;
        }
        try {
            return produtosCadastrados.pesquisar(procurado);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Localiza um produto na árvore de produtos organizados por id, a partir do
     * código de produto informado pelo usuário, e o retorna.
     * Em caso de não encontrar o produto, retorna null
     */
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        Integer id = lerOpcao("Digite o código do produto: ", Integer.class);
        if (id == null)
            return null;
        return localizarProduto(produtosCadastrados, id);
    }

    /**
     * Localiza um produto na árvore de produtos organizados por nome, a partir do
     * nome de produto informado pelo usuário, e o retorna.
     * A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna
     * null
     */
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome do produto: ");
        String nome = teclado.nextLine();
        if (nome == null || nome.trim().isEmpty())
            return null;
        nome = nome.trim().toLowerCase();
        return localizarProduto(produtosCadastrados, nome);
    }

    private static void mostrarProduto(Produto produto) {

        cabecalho();
        StringBuilder mensagem = new StringBuilder("Produto não encontrado.\n");

        if (produto != null) {
            mensagem = new StringBuilder(String.format("%s\n", produto));
        }

        System.out.println(mensagem.toString());
    }

    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static <K> void listarTodosOsProdutos(ABB<K, Produto> produtosCadastrados) {

        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        System.out.println(produtosCadastrados.toString());
    }

    /**
     * Localiza e remove um produto da árvore de produtos organizados por id, a
     * partir do código de produto informado pelo usuário, e o retorna.
     * Em caso de não encontrar o produto, retorna null
     */
    static Produto removerProdutoId(ABB<Integer, Produto> produtosCadastrados) {
        Integer id = lerOpcao("Digite o código do produto: ", Integer.class);
        if (id == null)
            return null;
        return removerProduto(produtosCadastrados, id);
    }

    /**
     * Localiza e remove um produto na árvore de produtos organizados por nome, a
     * partir do nome de produto informado pelo usuário, e o retorna.
     * A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna
     * null
     */
    static Produto removerProdutoNome(ABB<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome do produto: ");
        String nome = teclado.nextLine();
        if (nome == null || nome.trim().isEmpty())
            return null;
        nome = nome.trim().toLowerCase();
        return removerProduto(produtosCadastrados, nome);
    }

    static <K> Produto removerProduto(ABB<K, Produto> produtosCadastrados, K chave) {
        try {
            return produtosCadastrados.remover(chave);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            return null;
        }
    }

    private static <K> void recortarProduto(ABB<K, Produto> produtosCadastrados, K deOnde, K ateOnde) {
        try {
            Lista<Produto> lista = produtosCadastrados.recortar(deOnde, ateOnde);
            cabecalho();
            System.out.println("RESULTADO DO RECORTE:");
            System.out.println(lista.toString());
        } catch (IllegalArgumentException e) {
            cabecalho();
            System.out.println("Intervalo inválido: " + e.getMessage());
        }
    }

    private static void recortarProdutosNome(ABB<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome inicial do intervalo: ");
        String de = teclado.nextLine();
        System.out.println("Digite o nome final do intervalo: ");
        String ate = teclado.nextLine();
        if (de == null || ate == null || de.trim().isEmpty() || ate.trim().isEmpty()) {
            System.out.println("Intervalo inválido: nome inicial e final não podem ser vazios.");
            return;
        }
        de = de.trim().toLowerCase();
        ate = ate.trim().toLowerCase();
        recortarProduto(produtosCadastrados, de, ate);
    }

    private static void recortarProdutosId(ABB<Integer, Produto> produtosCadastrados) {
        Integer de = lerOpcao("Digite o código inicial do intervalo: ", Integer.class);
        if (de == null)
            return;
        Integer ate = lerOpcao("Digite o código final do intervalo: ", Integer.class);
        if (ate == null)
            return;
        recortarProduto(produtosCadastrados, de, ate);
    }

    private static void inserirNaTabela(Produto produto, Pedido pedido) {
        if (produto == null || pedido == null) {
            return;
        }

        Lista<Pedido> pedidos = pedidosPorProduto.get(produto);
        if (pedidos == null) {
            pedidos = new Lista<>();
            pedidosPorProduto.put(produto, pedidos);
        }
        pedidos.inserir(pedido);
    }

    static void pedidosDoProduto() {
        Produto produto = localizarProdutoNome(produtosCadastradosPorNome);
        if (produto == null) {
            cabecalho();
            System.out.println("Produto não encontrado.");
            return;
        }

        Lista<Pedido> pedidos = pedidosPorProduto.get(produto);
        if (pedidos == null || pedidos.vazia()) {
            cabecalho();
            System.out.println("Não há pedidos registrados para este produto.");
            return;
        }

        String nomeArquivo = String.format("pedidos_produto_%d.txt", produto.hashCode());
        try (PrintWriter escritor = new PrintWriter(new File(nomeArquivo), "UTF-8")) {
            escritor.println("RELATÓRIO DE PEDIDOS DO PRODUTO");
            escritor.println(produto.toString());
            escritor.println("--------------------------------------------------");
            escritor.println(pedidos.toString());
            cabecalho();
            System.out.println("Relatório gerado em: " + nomeArquivo);
        } catch (IOException e) {
            cabecalho();
            System.out.println("Erro ao gerar o relatório de pedidos: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao.toLowerCase()));
        if (produtosCadastradosPorNome == null) {
            produtosCadastradosPorNome = new ABB<>();
        }
        produtosCadastradosPorId = new ABB<Integer, Produto>(produtosCadastradosPorNome, (p -> p.idProduto));

        int opcao = -1;

        do {
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos(produtosCadastradosPorNome);
                case 2 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 3 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
                case 4 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
                case 5 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
                case 6 -> recortarProdutosNome(produtosCadastradosPorNome);
                case 7 -> recortarProdutosId(produtosCadastradosPorId);
                case 8 -> pedidosDoProduto();
                case 0 -> System.out.println("FLW VLW OBG VLT SMP.");
            }
            pausa();
        } while (opcao != 0);

        teclado.close();
    }
}