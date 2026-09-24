import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import core2d.Ponto2D;
import core3d.Mat4x4;
import core3d.Ponto3D;
import core3d.Triangulo3D;

public class MainCanvas extends JPanel implements Runnable {

    // ---------------- CONSTANTES ----------------
    int W = 640;
    int H = 480;
    int CENTRO_X = 320;
    int CENTRO_Y = 240;

    // ---------------- THREAD / LOOP ----------------
    Thread runner;
    boolean ativo = true;
    int paintcounter = 0;
    int framecount = 0;
    int fps = 0;
    int timer = 0;

    // ---------------- BUFFERS / IMAGEM ----------------
    BufferedImage imageBuffer;
    byte bufferDeVideo[];
    byte memoriaPlacaVideo[];
    short paleta[][];
    BufferedImage imgtmp = null;

    Font f = new Font("", Font.PLAIN, 30);

    // ---------------- MOUSE ----------------
    int clickX = 0, clickY = 0;
    int mouseX = 0, mouseY = 0;
    int eixoX = 0, eixoY = 0;

    int pixelSize = 0;
    int Largura = 0;
    int Altura = 0;

    // ---------------- ESTADO DO MUNDO ----------------
    ArrayList<Triangulo3D> listaDeTriangulos = new ArrayList<>();
    Ponto3D p0 = null;
    Ponto3D p1 = null;
    Ponto2D pC = new Ponto2D(320, 240);

    Mat4x4 projecao;
    Mat4x4 modelview;

    // ---------------- ESTADO DAS PROJEÇÕES ----------------
    // Guardamos qual projeção está ativa pra mostrar no HUD
    String nomeProjecaoAtual = "Ortográfica";
    float distanciaPerspectiva = 500f;

    // ---------------- TECLAS ----------------
    boolean LEFT = false;
    boolean RIGHT = false;
    boolean UP = false;
    boolean DOWN = false;

    // Filtros de cor (mantidos por compatibilidade)
    float filtroR = 1, filtroG = 1, filtroB = 1;

    // ---------------- CONSTRUTOR ----------------
    public MainCanvas() {

        // Leitura de debug de um BMP (opcional — mantido sem imprimir 64000 linhas)
        File fBmp = new File("imgbmp.bmp");
        try {
            FileInputStream fin = new FileInputStream(fBmp);
            byte todosodbytes[] = new byte[64000];
            int byteslidos = fin.read(todosodbytes);
            System.out.println("Bytes Lidos " + byteslidos);
            fin.close();
        } catch (FileNotFoundException e1) {
            // ok, arquivo pode não existir
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        setSize(W, H);
        setFocusable(true);

        Largura = W;
        Altura = H;
        pixelSize = W * H;

        imgtmp = loadImage("gato.jpg");

        imageBuffer = new BufferedImage(W, H, BufferedImage.TYPE_4BYTE_ABGR);
        bufferDeVideo = ((DataBufferByte) imageBuffer.getRaster().getDataBuffer()).getData();
        System.out.println("Buffer SIZE " + bufferDeVideo.length);

        // =========================================================
        //  TECLADO
        // =========================================================
        addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_W) UP = false;
                if (key == KeyEvent.VK_S) DOWN = false;
                if (key == KeyEvent.VK_A) LEFT = false;
                if (key == KeyEvent.VK_D) RIGHT = false;
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // ---- Movimento (W A S D) ----
                if (key == KeyEvent.VK_W) UP = true;
                if (key == KeyEvent.VK_S) DOWN = true;
                if (key == KeyEvent.VK_A) LEFT = true;
                if (key == KeyEvent.VK_D) RIGHT = true;

                // ---- Escala (Z / X) ----
                if (key == KeyEvent.VK_Z) {
                    Mat4x4 m = new Mat4x4();
                    m.setScale(0.8f, 0.8f, 0.8f);
                    modelview = modelview.multiplica(m);
                }
                if (key == KeyEvent.VK_X) {
                    Mat4x4 m = new Mat4x4();
                    m.setScale(1.2f, 1.2f, 1.2f);
                    modelview = modelview.multiplica(m);
                }

                // ---- Rotação em Y (Q / E) ----
                if (key == KeyEvent.VK_Q) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateY(-5);
                    modelview = modelview.multiplica(m);
                }
                if (key == KeyEvent.VK_E) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateY(+5);
                    modelview = modelview.multiplica(m);
                }

                // ---- Rotação em X (R / F) ----
                if (key == KeyEvent.VK_R) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateX(-5);
                    modelview = modelview.multiplica(m);
                }
                if (key == KeyEvent.VK_F) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateX(+5);
                    modelview = modelview.multiplica(m);
                }

                // ---- Rotação em Z (C / V) ----
                if (key == KeyEvent.VK_C) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateZ(-5);
                    modelview = modelview.multiplica(m);
                }
                if (key == KeyEvent.VK_V) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateZ(+5);
                    modelview = modelview.multiplica(m);
                }

                // ---- Rotação em eixo arbitrário (T / G) — eixo (1,1,0) ----
                if (key == KeyEvent.VK_T) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateAxis(-5, 1, 1, 0);
                    modelview = modelview.multiplica(m);
                }
                if (key == KeyEvent.VK_G) {
                    Mat4x4 m = new Mat4x4();
                    m.setRotateAxis(+5, 1, 1, 0);
                    modelview = modelview.multiplica(m);
                }

                // ---- Shearing (Y / H) — cisalha X em função de Y ----
                if (key == KeyEvent.VK_Y) {
                    Mat4x4 sh = new Mat4x4();
                    sh.setShear(0.1f, 0, 0, 0, 0, 0);
                    modelview = modelview.multiplica(sh);
                }
                if (key == KeyEvent.VK_H) {
                    Mat4x4 sh = new Mat4x4();
                    sh.setShear(-0.1f, 0, 0, 0, 0, 0);
                    modelview = modelview.multiplica(sh);
                }

                // ---- Reflexão (B) — espelha em X ----
                if (key == KeyEvent.VK_B) {
                    Mat4x4 m = new Mat4x4();
                    m.setScale(-1, 1, 1);
                    modelview = modelview.multiplica(m);
                }

                // ---- Projeções (1..4) ----
                if (key == KeyEvent.VK_1) {
                    projecao.setOrtographicProjection();
                    nomeProjecaoAtual = "Ortográfica";
                }
                if (key == KeyEvent.VK_2) {
                    projecao.setCavalierProjection();
                    nomeProjecaoAtual = "Oblíqua Cavalier";
                }
                if (key == KeyEvent.VK_3) {
                    projecao.setCabinetProjection();
                    nomeProjecaoAtual = "Oblíqua Cabinet";
                }
                if (key == KeyEvent.VK_4) {
                    projecao.setPerspectiveProjection(distanciaPerspectiva);
                    nomeProjecaoAtual = "Perspectiva d=" + (int)distanciaPerspectiva;
                }

                // ---- Ajuste da distância da perspectiva (5 / 6) ----
                if (key == KeyEvent.VK_5) {
                    distanciaPerspectiva = Math.max(50, distanciaPerspectiva - 50);
                    projecao.setPerspectiveProjection(distanciaPerspectiva);
                    nomeProjecaoAtual = "Perspectiva d=" + (int)distanciaPerspectiva;
                }
                if (key == KeyEvent.VK_6) {
                    distanciaPerspectiva += 50;
                    projecao.setPerspectiveProjection(distanciaPerspectiva);
                    nomeProjecaoAtual = "Perspectiva d=" + (int)distanciaPerspectiva;
                }

                // ---- Reset da modelview (Espaço) ----
                if (key == KeyEvent.VK_SPACE) {
                    modelview.setIdentity();
                }
            }
        });

        // =========================================================
        //  MOUSE
        // =========================================================
        addMouseListener(new MouseListener() {
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                clickX = e.getX();
                clickY = e.getY();

                if (e.getButton() == MouseEvent.BUTTON3) {
                    eixoX = clickX;
                    eixoY = clickY;
                }

                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Converte de tela para mundo (desfaz offset e flip Y)
                    float wx = clickX - CENTRO_X;
                    float wy = CENTRO_Y - clickY;

                    if (p0 == null) {
                        p0 = new Ponto3D(wx, wy, 0);
                    } else if (p1 == null) {
                        p1 = new Ponto3D(wx, wy, 0);
                    } else {
                        Ponto3D p2 = new Ponto3D(wx, wy, 0);
                        listaDeTriangulos.add(new Triangulo3D(p0, p1, p2));
                        p0 = null;
                        p1 = null;
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent arg0) {
                mouseX = arg0.getX();
                mouseY = arg0.getY();
            }
            @Override
            public void mouseDragged(MouseEvent arg0) {}
        });

        // =========================================================
        //  ESTADO INICIAL DAS MATRIZES
        // =========================================================
        modelview = new Mat4x4();
        modelview.setIdentity();

        projecao = new Mat4x4();
        projecao.setOrtographicProjection();

        // =========================================================
        //  CENA INICIAL
        // =========================================================
        criaCubo(100, 200, 0, 100, 100, 100);
        criaCubo(300, 200, 0, 100, 200, 100);

        ArrayList<Triangulo3D> listaDeTriangulosCarro = new ArrayList<>();
        criaCarro(listaDeTriangulosCarro);
        for (int i = 0; i < listaDeTriangulosCarro.size(); i++) {
            Triangulo3D tri = listaDeTriangulosCarro.get(i);
            Mat4x4 rot = new Mat4x4();
            tri.rotacao(rot);
            tri.escala(2, 2, 2);
            listaDeTriangulos.add(tri);
        }
    }

    // =========================================================
    //  GEOMETRIA
    // =========================================================
    private void criaCubo(float x, float y, float z, float lx, float ly, float lz) {
        Ponto3D p1 = new Ponto3D(x, y, z);
        Ponto3D p2 = new Ponto3D(x + lx, y, z);
        Ponto3D p3 = new Ponto3D(x + lx, y + ly, z);
        Ponto3D p4 = new Ponto3D(x, y + ly, z);

        Ponto3D p5 = new Ponto3D(x, y, z + lz);
        Ponto3D p6 = new Ponto3D(x + lx, y, z + lz);
        Ponto3D p7 = new Ponto3D(x + lx, y + ly, z + lz);
        Ponto3D p8 = new Ponto3D(x, y + ly, z + lz);

        // Frente
        listaDeTriangulos.add(new Triangulo3D(p1, p2, p3));
        listaDeTriangulos.add(new Triangulo3D(p1, p3, p4));
        // Trás
        listaDeTriangulos.add(new Triangulo3D(p5, p6, p7));
        listaDeTriangulos.add(new Triangulo3D(p5, p7, p8));
        // Esquerda
        listaDeTriangulos.add(new Triangulo3D(p1, p4, p8));
        listaDeTriangulos.add(new Triangulo3D(p1, p8, p5));
        // Direita
        listaDeTriangulos.add(new Triangulo3D(p2, p6, p7));
        listaDeTriangulos.add(new Triangulo3D(p2, p7, p3));
        // Topo
        listaDeTriangulos.add(new Triangulo3D(p4, p3, p7));
        listaDeTriangulos.add(new Triangulo3D(p4, p7, p8));
        // Base
        listaDeTriangulos.add(new Triangulo3D(p1, p5, p6));
        listaDeTriangulos.add(new Triangulo3D(p1, p6, p2));
    }

    private void criaRoda(ArrayList<Triangulo3D> listtri, float x, float y, float z) {
        float raio = 9;
        Ponto3D centro = new Ponto3D(x, y, z);
        Ponto3D cima     = new Ponto3D(x, y, z + raio);
        Ponto3D direita  = new Ponto3D(x + raio, y, z);
        Ponto3D baixo    = new Ponto3D(x, y, z - raio);
        Ponto3D esquerda = new Ponto3D(x - raio, y, z);

        listtri.add(new Triangulo3D(centro, cima, direita));
        listtri.add(new Triangulo3D(centro, direita, baixo));
        listtri.add(new Triangulo3D(centro, baixo, esquerda));
        listtri.add(new Triangulo3D(centro, esquerda, cima));
    }

    private void criaCarro(ArrayList<Triangulo3D> listtri) {
        // Carroceria
        Ponto3D p1 = new Ponto3D(8, 20, 15);
        Ponto3D p2 = new Ponto3D(8, 20, 27);
        Ponto3D p3 = new Ponto3D(35, 20, 31);
        Ponto3D p4 = new Ponto3D(90, 20, 28);
        Ponto3D p5 = new Ponto3D(92, 20, 15);

        Ponto3D p6  = new Ponto3D(8, 80, 15);
        Ponto3D p7  = new Ponto3D(8, 80, 27);
        Ponto3D p8  = new Ponto3D(35, 80, 31);
        Ponto3D p9  = new Ponto3D(90, 80, 28);
        Ponto3D p10 = new Ponto3D(92, 80, 15);

        listtri.add(new Triangulo3D(p1, p2, p3));
        listtri.add(new Triangulo3D(p1, p3, p5));
        listtri.add(new Triangulo3D(p3, p4, p5));

        listtri.add(new Triangulo3D(p6, p8, p7));
        listtri.add(new Triangulo3D(p6, p10, p8));
        listtri.add(new Triangulo3D(p8, p10, p9));

        listtri.add(new Triangulo3D(p1, p6, p7));
        listtri.add(new Triangulo3D(p1, p7, p2));

        listtri.add(new Triangulo3D(p5, p4, p9));
        listtri.add(new Triangulo3D(p5, p9, p10));

        listtri.add(new Triangulo3D(p1, p5, p10));
        listtri.add(new Triangulo3D(p1, p10, p6));

        // Cabine
        Ponto3D c1 = new Ponto3D(35, 20, 31);
        Ponto3D c2 = new Ponto3D(48, 20, 48);
        Ponto3D c3 = new Ponto3D(72, 20, 48);
        Ponto3D c4 = new Ponto3D(84, 20, 28);

        Ponto3D c5 = new Ponto3D(35, 80, 31);
        Ponto3D c6 = new Ponto3D(48, 80, 48);
        Ponto3D c7 = new Ponto3D(72, 80, 48);
        Ponto3D c8 = new Ponto3D(84, 80, 28);

        listtri.add(new Triangulo3D(c1, c2, c3));
        listtri.add(new Triangulo3D(c1, c3, c4));
        listtri.add(new Triangulo3D(c5, c7, c6));
        listtri.add(new Triangulo3D(c5, c8, c7));
        listtri.add(new Triangulo3D(c1, c5, c6));
        listtri.add(new Triangulo3D(c1, c6, c2));
        listtri.add(new Triangulo3D(c4, c3, c7));
        listtri.add(new Triangulo3D(c4, c7, c8));
        listtri.add(new Triangulo3D(c2, c6, c7));
        listtri.add(new Triangulo3D(c2, c7, c3));
        listtri.add(new Triangulo3D(c1, c4, c8));
        listtri.add(new Triangulo3D(c1, c8, c5));

        // Rodas
        criaRoda(listtri, 25, 20, 15);
        criaRoda(listtri, 25, 80, 15);
        criaRoda(listtri, 75, 20, 15);
        criaRoda(listtri, 75, 80, 15);
    }

    // =========================================================
    //  BUFFER DE PIXELS (mantido, opcional)
    // =========================================================
    private void drawImageToBuffer(BufferedImage image, int x, int y, float fr, float fg, float fb) {
        byte[] imgBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        int iw = image.getWidth();
        int ih = image.getHeight();

        for (int yi = 0; yi < ih; yi++) {
            for (int xi = 0; xi < iw; xi++) {
                int pixi = yi * iw * 4 + xi * 4;
                int pixb = (yi + y) * W * 4 + (xi + x) * 4;

                bufferDeVideo[pixb] = imgBuffer[pixi];

                int b = (imgBuffer[pixi + 1] & 0xff);
                int g = (imgBuffer[pixi + 2] & 0xff);
                int r = (imgBuffer[pixi + 3] & 0xff);

                b = Math.min(255, (int)(b * fb));
                g = Math.min(255, (int)(g * fg));
                r = Math.min(255, (int)(r * fr));

                bufferDeVideo[pixb + 1] = (byte)(b & 0xff);
                bufferDeVideo[pixb + 2] = (byte)(g & 0xff);
                bufferDeVideo[pixb + 3] = (byte)(r & 0xff);
            }
        }
    }

    public void desenhaLinhaHorizontal(int x, int y, int w) {
        int pospix = y * (W * 4) + x * 4;
        for (int i = 0; i < w; i++) {
            bufferDeVideo[pospix]     = (byte)255;
            bufferDeVideo[pospix + 1] = (byte)0;
            bufferDeVideo[pospix + 2] = (byte)0;
            bufferDeVideo[pospix + 3] = (byte)255;
            pospix += 4;
        }
    }

    public void desenhaLinhaVertical(int x, int y, int h) {
        int pospix = y * (W * 4) + x * 4;
        for (int i = 0; i < h; i++) {
            bufferDeVideo[pospix]     = (byte)255;
            bufferDeVideo[pospix + 1] = (byte)0;
            bufferDeVideo[pospix + 2] = (byte)0;
            bufferDeVideo[pospix + 3] = (byte)255;
            pospix += (W * 4);
        }
    }

    public void desenhaPixel(int x, int y, int r, int g, int b) {
        int pospix = y * (W * 4) + x * 4;
        bufferDeVideo[pospix]     = (byte)255;
        bufferDeVideo[pospix + 1] = (byte)(b & 0xff);
        bufferDeVideo[pospix + 2] = (byte)(g & 0xff);
        bufferDeVideo[pospix + 3] = (byte)(r & 0xff);
    }

    // =========================================================
    //  SIMULAÇÃO (movimento W A S D)
    // =========================================================
    public void simulaMundo(long diftime) {
        float difS = diftime / 1000.0f;
        float vel = 50;

        timer += diftime;

        float dx = 0, dy = 0;
        if (UP)    dy += 1;
        if (DOWN)  dy -= 1;
        if (LEFT)  dx -= 1;
        if (RIGHT) dx += 1;

        if (dx != 0 || dy != 0) {
            Mat4x4 matrot = new Mat4x4();
            matrot.setTranslate(dx * vel * difS, dy * vel * difS, 0);
            modelview = modelview.multiplica(matrot);
        }
    }

    // =========================================================
    //  RENDER
    // =========================================================
    @Override
    public void paint(Graphics g) {
        // Limpa buffer de pixels (opcional, não usado no desenho atual)
        for (int i = 0; i < bufferDeVideo.length; i++) {
            bufferDeVideo[i] = 0;
        }

        g.setFont(f);

        // Fundo
        g.setColor(Color.white);
        g.fillRect(0, 0, 800, 600);

        // Marcador da origem (botão direito do mouse)
        g.setColor(Color.blue);
        g.fillRect(eixoX - 2, eixoY - 2, 5, 5);

        // Eixos (opcional, ajuda a visualizar)
        g.setColor(new Color(220, 220, 220));
        g.drawLine(CENTRO_X, 0, CENTRO_X, H);
        g.drawLine(0, CENTRO_Y, W, CENTRO_Y);

        // Triângulos 3D
        g.setColor(Color.black);
        for (int i = 0; i < listaDeTriangulos.size(); i++) {
            Triangulo3D tri = listaDeTriangulos.get(i);
            tri.desenhase((Graphics2D) g, modelview, projecao);
        }

        // Pré-visualização do triângulo em construção
        g.setColor(Color.red);
        if (p0 != null) {
            int p0x = (int)(p0.x + CENTRO_X);
            int p0y = (int)(CENTRO_Y - p0.y);
            if (p1 != null) {
                int p1x = (int)(p1.x + CENTRO_X);
                int p1y = (int)(CENTRO_Y - p1.y);
                g.drawLine(p0x, p0y, p1x, p1y);
                g.drawLine(p0x, p0y, mouseX, mouseY);
                g.drawLine(p1x, p1y, mouseX, mouseY);
            } else {
                g.drawLine(p0x, p0y, mouseX, mouseY);
            }
        }

        // HUD
        g.setColor(Color.black);
        g.drawString("FPS " + fps + "  mouse: " + mouseX + "," + mouseY, 10, 25);
        g.drawString("Projeção: " + nomeProjecaoAtual, 10, 55);
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
            paintcounter += 100;

            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long newtime = System.currentTimeMillis();
            long novoSegundo = newtime / 1000;
            diftime = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            framecount++;

            if (novoSegundo != segundo) {
                fps = framecount;
                framecount = 0;
                segundo = novoSegundo;
            }
        }
    }

    // =========================================================
    //  IMAGEM
    // =========================================================
    public BufferedImage loadImage(String filename) {
        try {
            imgtmp = ImageIO.read(new File(filename));
            BufferedImage imgout = new BufferedImage(
                imgtmp.getWidth(), imgtmp.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            imgout.getGraphics().drawImage(imgtmp, 0, 0, null);
            imgtmp = null;
            return imgout;
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
}
