package core3d;

import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * Um objeto da cena: um conjunto de triângulos + uma matriz de mundo
 * própria (posição/escala/rotação na cena).
 */
public class Objeto3D {

    public ArrayList<Triangulo3D> triangulos = new ArrayList<>();
    public Mat4x4 mundo = new Mat4x4();

    public Objeto3D() {
        mundo.setIdentity();
    }

    public void setPosicao(float x, float y, float z) {
        Mat4x4 t = new Mat4x4();
        t.setTranslate(x, y, z);
        mundo = t; // ou mundo.multiplica(t) se quiser compor
    }

    public void setRotacaoY(float ang) {
        Mat4x4 r = new Mat4x4();
        r.setRotateY(ang);
        mundo = mundo.multiplica(r);
    }

    public void setEscala(float s) {
        Mat4x4 e = new Mat4x4();
        e.setScale(s, s, s);
        mundo = mundo.multiplica(e);
    }

    public void desenhase(Graphics2D g, Mat4x4 view, Mat4x4 proj) {
        for (int i = 0; i < triangulos.size(); i++) {
            triangulos.get(i).desenhase(g, mundo, view, proj);
        }
    }
}
