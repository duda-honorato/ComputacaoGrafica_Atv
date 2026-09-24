package core3d;

import java.awt.Graphics2D;

public class Triangulo3D {
    Ponto3D pa;
    Ponto3D pb;
    Ponto3D pc;

    public Triangulo3D(Ponto3D a, Ponto3D b, Ponto3D c) {
        super();
        this.pa = new Ponto3D(a);
        this.pb = new Ponto3D(b);
        this.pc = new Ponto3D(c);
    }

    /** Desenho direto (útil para debug ou quando os pontos já estão em tela). */
    public void desenhase(Graphics2D dbg) {
        dbg.drawLine((int)pa.x,(int)pa.y,(int)pb.x,(int)pb.y);
        dbg.drawLine((int)pb.x,(int)pb.y,(int)pc.x,(int)pc.y);
        dbg.drawLine((int)pc.x,(int)pc.y,(int)pa.x,(int)pa.y);
    }

    /**
     * Desenho com pipeline completo:
     *   mundo -> modelview -> projeção -> normalização -> tela
     *
     * Aplica as duas matrizes em cada ponto e depois converte para
     * coordenadas de tela com:
     *   - offset do centro (320, 240)
     *   - flip do eixo Y (Java2D cresce pra baixo, mundo cresce pra cima)
     */
    public void desenhase(Graphics2D dbg, Mat4x4 modelview, Mat4x4 projection) {
        // >>> ALTERADO: pipeline correto (MV seguido de projeção)
        Ponto3D pa2 = projection.multiplicaPonto(modelview.multiplicaPonto(pa));
        Ponto3D pb2 = projection.multiplicaPonto(modelview.multiplicaPonto(pb));
        Ponto3D pc2 = projection.multiplicaPonto(modelview.multiplicaPonto(pc));

        // >>> NOVO: offset do centro + flip do Y
        int cx = 320, cy = 240;

        int ax = (int)(pa2.x + cx), ay = (int)(cy - pa2.y);
        int bx = (int)(pb2.x + cx), by = (int)(cy - pb2.y);
        int ccx = (int)(pc2.x + cx), ccy = (int)(cy - pc2.y);

        dbg.drawLine(ax, ay, bx, by);
        dbg.drawLine(bx, by, ccx, ccy);
        dbg.drawLine(ccx, ccy, ax, ay);
    }

    // -------- Transformações locais (mutam os pontos do triângulo) --------

    public void translacao(float a, float b, float c) {
        Mat4x4 m = new Mat4x4();
        m.setTranslate(a, b, c);
        pa.multiplicaMat(m);
        pb.multiplicaMat(m);
        pc.multiplicaMat(m);
    }

    public void escala(float a, float b, float c) {
        Mat4x4 m = new Mat4x4();
        m.setScale(a, b, c); // >>> ALTERADO: era setSacale
        pa.multiplicaMat(m);
        pb.multiplicaMat(m);
        pc.multiplicaMat(m);
    }

    public void rotacao(float ang) {
        Mat4x4 m = new Mat4x4();
        m.setRotateY(ang);
        pa.multiplicaMat(m);
        pb.multiplicaMat(m);
        pc.multiplicaMat(m);
    }

    public void rotacao(Mat4x4 m) {
        pa.multiplicaMat(m);
        pb.multiplicaMat(m);
        pc.multiplicaMat(m);
    }
}
