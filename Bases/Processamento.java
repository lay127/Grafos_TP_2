import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

class ArestaDirecionada {
    private int v, w;
    private Double peso;

    public ArestaDirecionada(int v, int w, Double peso) {
        this.v = v;
        this.w = w;
        this.peso = peso;
    }

    public int getV() {
        return v;
    }

    public int getW() {
        return w;
    }

    public Double getPeso() {
        return peso;
    }
}

class GrafoDirecionado {
    private int V, E;
    private ArestaDirecionada[][] adj;

    GrafoDirecionado(int n){
        this.V = n+1;
        this.E = 0;
        this.adj = new ArestaDirecionada[V][V];
        for(int i = 0; i < V; i++){
            for (int j = 0; j < V; j++) {
                adj[i][j] = new ArestaDirecionada(i, j, null);
            }
        }
    }

    public void adicionarAresta(int v, int u, double w) {
        if ((validarVertice(v)) && (validarVertice(u))){
            adj[v][u] = new ArestaDirecionada(v, u, w);
            E++;
        }
    }

    public void mostrar(){
        for(int i = 1; i < V; i++){
            for (int j = 1; j < V; j++) {
                if (adj[i][j].getPeso() == null) {
                    if (i == j){
                        System.out.printf("%d ", 0);
                    } else {
                        System.out.printf("inf ");
                    }
                } else {
                    System.out.printf("%.2f ", adj[i][j].getPeso());
                }
            }
            System.out.println();
        }
    }

    private boolean validarVertice(int v) {
        return (v != 0) && (v <= V);
    }

    public int getV() {
        return V;
    }

    public int getE() {
        return E;
    }

    public ArestaDirecionada[][] getAdj() {
        return adj;
    }
    
}

class FloydWarshall {
    private boolean hasNegativeCycle;
    private double[][] distTo; // tamanho do caminho mais curto de v para w
    private ArestaDirecionada[][] edgeTo; // ultima aresta no caminho mais curto de v para w

    public FloydWarshall(GrafoDirecionado G) {
        int V = G.getV();
        distTo = new double[V][V];
        edgeTo = new ArestaDirecionada[V][V];

        // inicializar distancias com valor infinito
        for (int v = 1; v < V; v++) {
            for (int w = 1; w < V; w++) {
                distTo[v][w] = Double.POSITIVE_INFINITY;
            }
        }

        // inicializar distancias com valor do grafo direcionado
        for (int v = 1; v < G.getV(); v++) {
            for (ArestaDirecionada e : G.getAdj()[v]) {
                if (e.getPeso() != null) distTo[e.getV()][e.getW()] = e.getPeso();
                edgeTo[e.getV()][e.getW()] = e;
            }
            // caminho de v para v (vertice com ele mesmo)
            if (distTo[v][v] >= 0.0) {
                distTo[v][v] = 0.0;
                edgeTo[v][v] = null;
            }
        }

        // atualização de distâncias
        for (int i = 1; i < V; i++) {
            for (int v = 1; v < V; v++) {
                if (edgeTo[v][i] == null) continue;
                for (int w = 1; w < V; w++) {
                    if (distTo[v][w] > distTo[v][i] + distTo[i][w]) {
                        distTo[v][w] = distTo[v][i] + distTo[i][w];
                        edgeTo[v][w] = edgeTo[i][w];
                    }
                }
                // checar ciclo negativo
                if (distTo[v][v] < 0.0) {
                    hasNegativeCycle = true;
                    return;
                }
            }
        }
        assert check(G);
    }

    private boolean check(GrafoDirecionado G) {
        // caso nao exista ciclo de custo de custo negativo no grafo...
        if (!hasNegativeCycle()) {
            for (int v = 1; v < G.getV(); v++) {
                for (ArestaDirecionada e : G.getAdj()[v]) {
                    int w = e.getW();
                    for (int i = 1; i < G.getV(); i++) {
                        if (distTo[i][w] > distTo[i][v] + e.getPeso()) {
                            System.err.println("edge " + e + " is eligible");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void validarVertice(int v) {
        int V = distTo.length;
        if (v <= 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    public boolean hasNegativeCycle() {
        return hasNegativeCycle;
    }

    public boolean hasPath(int v, int w) {
        validarVertice(v);
        validarVertice(w);
        return distTo[v][w] < Double.POSITIVE_INFINITY;
    }

    public double dist(int v, int w) {
        validarVertice(v);
        validarVertice(w);
        if (hasNegativeCycle())
            throw new UnsupportedOperationException("Existe um ciclo de custo negativo.");
        return distTo[v][w];
    }

}

public class Processamento {
    public static void main(String[] args) throws Exception {
        // inicializar variaveis
        int n = 0, m = 0, k = 0;
        String padrao = "Original/pmed%d.txt";

        // extrair dados de cada arquivo para construção dos grafos
        try {
            for (int i = 1; i < 41; i++) {
                // acessar arquivo
                File arquivo = new File(String.format(padrao, i));                
                Scanner sc = new Scanner(arquivo);
                
                // ler n, m, k do arquivo
                n = sc.nextInt(); m = sc.nextInt(); k = sc.nextInt();
                
                // inicializar grafo com n° de vertices
                GrafoDirecionado G = new GrafoDirecionado(n);
                
                // ler resto do arquivo
                sc.nextLine(); // pular newline
                for (int j = 0; j < m; j++){
                    int origem = sc.nextInt();
                    int destino = sc.nextInt();
                    Double peso = sc.nextDouble();
                    G.adicionarAresta(origem, destino, peso);
                }
                sc.close();

                // aplicar Floyd-Warshall
                FloydWarshall spt = new FloydWarshall(G);
                
                // criar arquivo para armazenar grafo processado
                String padraoP = "pmed Processado/pmed-p%d.txt";
                File arqP = new File(String.format(padraoP, i));

                try (FileWriter writer = new FileWriter(arqP)) {
                    // escrever primeira linha do arquivo com info original do grafo
                    writer.write(String.format("%d %d %d\n", n, m, k));
                    // escrever os caminhos minimos de v para os demais vertices w
                    for (int v = 1; v < G.getV(); v++) {
                        for (int w = 1; w < G.getV(); w++) {
                            if (spt.hasPath(v, w)) {
                                writer.write(String.format("%d %d %d\n", v, w, Math.round(spt.dist(v, w))));
                            } 
                            // se nao existir caminho, informar valor como infinito
                            else {
                                writer.write(String.format("%d %d inf\n", v, w));
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Um erro ocorreu.");
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        
    }

}
