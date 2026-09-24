import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import core3d.Mat4x4;
import core3d.Objeto3D;
import core3d.ObjLoader;
import core3d.Ponto3D;

public class MainCanvas extends JPanel implements Runnable {

    // ---------------- TELA ----------------
    int W = 640;
    int H = 480;
    int CENTRO_X = 320;
    int CENTRO_Y = 240;

    // ---------------- THREAD ----------------
    Thread runner;
    boolean ativo = true;
    int framecount = 0;
    int fps = 0;

    // ---------------- CENA ----------------
    ArrayList<Objeto3D> cena = new ArrayList<>();

    // ---------------- CÂMERA ----------------
    Ponto3D camPos = new Ponto3D(0, 0, 600); // posição no mundo
    float yaw   = 0f;   // graus (rotação em Y)
    float pitch = 0f;   // graus (rotação em X)

    // Limite: câmera não pode se afastar além deste raio do centro da cena
    float RAIO_MAX = 1500f;

    // ---------------- PROJEÇÃO ----------------
    Mat4x4 projecao;

    // ---------------- TECLAS ----------------
    boolean W_KEY, A_KEY, S_KEY, D_KEY, Q_KEY, E_KEY;
    boolean UP_KEY, DOWN_KEY, LEFT_KEY, RIGHT_KEY;

    Font fonte = new Font("Arial", Font.PLAIN, 14);

    // =========================================================
    // CONSTRUTOR
    // =========================================================
    public MainCanvas() {
        setSize(W, H);
        setFocusable(true);

        projecao = new Mat4x4();
        projecao.setOrtographicProjection();

        // -------- Cena --------
        // Tamanhos fixos após normalização:
        float TAM_CASA = 300f;
        float TAM_CADEIRA = 150f;

        Objeto3D casa    = ObjLoader.carregar("obj_1/medieval house.obj", TAM_CASA);
        Objeto3D cadeira = ObjLoader.carregar("obj_1/chair_01.obj", TAM_CADEIRA);

        // Casa principal no centro
        Objeto3D casa1 = clonar(casa);
        casa1.setPosicao(0, 0, 0);
        cena.add(casa1);

        // Cadeiras ao redor da casa
        posicionarCadeira(cadeira, -250, 0,  200,  30);
        posicionarCadeira(cadeira,  250, 0,  200, -30);
        posicionarCadeira(cadeira, -250, 0, -200,  60);
        posicionarCadeira(cadeira,  250, 0, -200, -60);

        // Segunda casa ao fundo
        Objeto3D casa2 = clonar(casa);
        casa2.setPosicao(0, 0, -500);
        casa2.setRotacaoY(180);
        cena.add(casa2);

        // -------- Teclado --------
        addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: W_KEY = true; break;
                    case KeyEvent.VK_S: S_KEY = true; break;
                    case KeyEvent.VK_A: A_KEY = true; break;
                    case KeyEvent.VK_D: D_KEY = true; break;
                    case KeyEvent.VK_Q: Q_KEY = true; break;
                    case KeyEvent.VK_E: E_KEY = true; break;
                    case KeyEvent.VK_UP:    UP_KEY = true; break;
                    case KeyEvent.VK_DOWN:  DOWN_KEY = true; break;
                    case KeyEvent.VK_LEFT:  LEFT_KEY = true; break;
                    case KeyEvent.VK_RIGHT: RIGHT_KEY = true; break;
                    case KeyEvent.VK_SPACE:
                        // Reset câmera
                        camPos = new Ponto3D(0, 0, 600);
                        yaw = 0; pitch = 0;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: W_KEY = false; break;
                    case KeyEvent.VK_S: S_KEY = false; break;
                    case KeyEvent.VK_A: A_KEY = false; break;
                    case KeyEvent.VK_D: D_KEY = false; break;
                    case KeyEvent.VK_Q: Q_KEY = false; break;
                    case KeyEvent.VK_E: E_KEY = false; break;
                    case KeyEvent.VK_UP:    UP_KEY = false; break;
                    case KeyEvent.VK_DOWN:  DOWN_KEY = false; break;
                    case KeyEvent.VK_LEFT:  LEFT_KEY = false; break;
                    case KeyEvent.VK_RIGHT: RIGHT_KEY = false; break;
                }
            }
        });
    }

    // Clona um Objeto3D (para reaproveitar o mesmo OBJ várias vezes)
    private Objeto3D clonar(Objeto3D src) {
        Objeto3D o = new Objeto3D();
        o.triangulos = src.triangulos; // compartilha a geometria (ok, é imutável aqui)
        o.mundo = new Mat4x4();
        return o;
    }

    private void posicionarCadeira(Objeto3D modelo, float x, float y, float z, float rotY) {
        Objeto3D c = clonar(modelo);
        c.setPosicao(x, y, z);
        if (rotY != 0) c.setRotacaoY(rotY);
        cena.add(c);
    }

    // =========================================================
    //  MATRIZ DE VIEW (inversa da câmera)
    // =========================================================
    private Mat4x4 construirView() {
        // View = R(-pitch) * R(-yaw) * T(-camPos)
        Mat4x4 t = new Mat4x4();
        t.setTranslate(-camPos.x, -camPos.y, -camPos.z);

        Mat4x4 ry = new Mat4x4();
        ry.setRotateY(-yaw);

        Mat4x4 rx = new Mat4x4();
        rx.setRotateX(-pitch);

        return rx.multiplica(ry).multiplica(t);
    }

    // =========================================================
    //  SIMULAÇÃO (movimento da câmera)
    // =========================================================
    public void simulaMundo(long diftime) {
        float difS = diftime / 1000.0f;
        if (difS > 0.1f) difS = 0.1f; // trava anti-lag

        float velMove  = 400f; // unidades por segundo
        float velGiro  = 90f;  // graus por segundo

        // --- Rotação (setas) ---
        if (LEFT_KEY)  yaw   -= velGiro * difS;
        if (RIGHT_KEY) yaw   += velGiro * difS;
        if (UP_KEY)    pitch += velGiro * difS;
        if (DOWN_KEY)  pitch -= velGiro * difS;

        if (pitch >  89) pitch =  89;
        if (pitch < -89) pitch = -89;

        // --- Movimento (WASD) ---
        // direção "frente" no plano XZ, baseada no yaw
        double yawRad = Math.toRadians(yaw);
        float fx = (float) Math.sin(yawRad);
        float fz = -(float) Math.cos(yawRad);

        // direção "direita" = perpendicular
        float rx = (float) Math.cos(yawRad);
        float rz = (float) Math.sin(yawRad);

        float dx = 0, dy = 0, dz = 0;

        if (W_KEY) { dx += fx; dz += fz; }
        if (S_KEY) { dx -= fx; dz -= fz; }
        if (D_KEY) { dx += rx; dz += rz; }
        if (A_KEY) { dx -= rx; dz -= rz; }
        if (Q_KEY) { dy -= 1; }   // desce
        if (E_KEY) { dy += 1; }   // sobe

        // normaliza o vetor horizontal pra não andar mais rápido na diagonal
        float lenH = (float) Math.sqrt(dx*dx + dz*dz);
        if (lenH > 1e-6f) {
            dx /= lenH; dz /= lenH;
        }

        float passo = velMove * difS;

        Ponto3D nova = new Ponto3D(
            camPos.x + dx * passo,
            camPos.y + dy * passo,
            camPos.z + dz * passo
        );

        // --- Limite: câmera não sai da esfera da cena ---
        float dist = (float) Math.sqrt(nova.x*nova.x + nova.y*nova.y + nova.z*nova.z);
        if (dist > RAIO_MAX) {
            float fator = RAIO_MAX / dist;
            nova.x *= fator;
            nova.y *= fator;
            nova.z *= fator;
        }

        camPos = nova;
    }

    // =========================================================
    //  PAINT
    // =========================================================
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Fundo
        g2.setColor(new Color(230, 240, 250));
        g2.fillRect(0, 0, W, H);

        Mat4x4 view = construirView();

        // --- Desenha a cena ---
        g2.setColor(Color.black);
        for (int i = 0; i < cena.size(); i++) {
            cena.get(i).desenhase(g2, view, projecao);
        }

        // --- HUD ---
        g2.setFont(fonte);
        g2.setColor(Color.black);
        g2.drawString("FPS: " + fps, 10, 20);
        g2.drawString(String.format("Cam: (%.0f, %.0f, %.0f)  yaw=%.0f pitch=%.0f",
                camPos.x, camPos.y, camPos.z, yaw, pitch), 10, 38);
        g2.drawString("WASD mover | Setas olhar | Q/E subir/descer | Espaço reset", 10, 56);
        g2.drawString("Objetos na cena: " + cena.size(), 10, 74);
    }

    // =========================================================
    //  THREAD
    // =========================================================
    public void start() {
        runner = new Thread(this);
        runner.start();
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long segundo = time / 1000;
        long diftime = 0;

        while (ativo) {
            simulaMundo(diftime);
            paintImmediately(0, 0, W, H);

            try { Thread.sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }

            long newtime = System.currentTimeMillis();
            long novoSegundo = newtime / 1000;
            diftime = newtime - time;
            time = newtime;
            framecount++;

            if (novoSegundo != segundo) {
                fps = framecount;
                framecount = 0;
                segundo = novoSegundo;
            }
        }
    }
}
