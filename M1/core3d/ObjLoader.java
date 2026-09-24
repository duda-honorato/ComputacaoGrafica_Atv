package core3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Carregador simples de arquivos .obj.
 * - Lê apenas "v" (vértices) e "f" (faces).
 * - Ignora vt, vn, vp e qualquer outra diretiva.
 * - Suporta formatos: f v1 v2 v3, f v1/vt1 v2/vt2 v3/vt3, f v1//vn1 ...
 * - Suporta índices negativos (relativos ao fim da lista).
 * - Triangula polígonos (fan) — quads viram 2 triângulos.
 * - Ao final, normaliza: centraliza no centroide e escala para tamanhoAlvo.
 */
public class ObjLoader {

    /** Carrega e normaliza para um "tamanho" (maior dimensão) fixo. */
    public static Objeto3D carregar(String caminho, float tamanhoAlvo) {
        Objeto3D obj = new Objeto3D();

        ArrayList<Ponto3D> vertices = new ArrayList<>();
        ArrayList<int[]> faces = new ArrayList<>(); // índices (0-based)

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(caminho)));
            String linha;

            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;

                String[] partes = linha.split("\\s+");
                String tag = partes[0];

                if (tag.equals("v")) {
                    // v x y z  (opcional w)
                    float x = Float.parseFloat(partes[1]);
                    float y = Float.parseFloat(partes[2]);
                    float z = Float.parseFloat(partes[3]);
                    vertices.add(new Ponto3D(x, y, z));

                } else if (tag.equals("f")) {
                    // Pode ter 3 ou mais vértices (polígono). Triangula em leque.
                    int n = partes.length - 1;
                    int[] idx = new int[n];

                    for (int i = 0; i < n; i++) {
                        String token = partes[i + 1];
                        // pega só a parte antes da primeira barra
                        String vStr = token.split("/")[0];
                        int vi = Integer.parseInt(vStr);

                        // índice negativo = relativo ao fim
                        if (vi < 0) vi = vertices.size() + vi;
                        else vi = vi - 1; // OBJ é 1-based

                        idx[i] = vi;
                    }

                    // Triangulação em leque: (0,1,2), (0,2,3), (0,3,4)...
                    for (int i = 1; i < n - 1; i++) {
                        faces.add(new int[]{ idx[0], idx[i], idx[i + 1] });
                    }
                }
                // qualquer outra tag (vt, vn, vp, g, o, s, usemtl, mtllib...) é ignorada
            }
            br.close();

        } catch (IOException e) {
            System.out.println("Erro ao ler OBJ: " + caminho);
            e.printStackTrace();
            return obj;
        }

        if (vertices.isEmpty()) {
            System.out.println("OBJ vazio ou sem vértices: " + caminho);
            return obj;
        }

        // -------- Normalização: centraliza + escala --------
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Ponto3D p : vertices) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.z < minZ) minZ = p.z;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
            if (p.z > maxZ) maxZ = p.z;
        }

        float cx = (minX + maxX) * 0.5f;
        float cy = (minY + maxY) * 0.5f;
        float cz = (minZ + maxZ) * 0.5f;

        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;
        float maior = Math.max(dx, Math.max(dy, dz));
        if (maior == 0) maior = 1;

        float escala = tamanhoAlvo / maior;

        // Aplica em cada vértice
        for (Ponto3D p : vertices) {
            p.x = (p.x - cx) * escala;
            p.y = (p.y - cy) * escala;
            p.z = (p.z - cz) * escala;
        }

        // -------- Cria triângulos --------
        for (int[] f : faces) {
            Ponto3D a = vertices.get(f[0]);
            Ponto3D b = vertices.get(f[1]);
            Ponto3D c = vertices.get(f[2]);
            obj.triangulos.add(new Triangulo3D(a, b, c));
        }

        System.out.println("OBJ carregado: " + caminho
                + "  vértices=" + vertices.size()
                + "  triângulos=" + obj.triangulos.size());

        return obj;
    }
}
