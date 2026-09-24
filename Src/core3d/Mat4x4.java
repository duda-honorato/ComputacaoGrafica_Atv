package core3d;

public class Mat4x4 {
    float mat[][] = new float[4][4];

    public Mat4x4() {
        setIdentity();
    }

    public void setIdentity() {
        zera();
        mat[0][0] = 1;
        mat[1][1] = 1;
        mat[2][2] = 1;
        mat[3][3] = 1;
    }

    public void zera() {
        for (int y = 0; y < 4; y++)
            for (int x = 0; x < 4; x++)
                mat[y][x] = 0;
    }

    // ---------------- TRANSFORMAÇÕES BÁSICAS ----------------

    public void setTranslate(float a, float b, float c) {
        setIdentity();
        mat[0][3] = a;
        mat[1][3] = b;
        mat[2][3] = c;
    }

    // Corrigido: era setSacale (typo). Mantive o antigo por compatibilidade.
    public void setScale(float a, float b, float c) {
        setIdentity();
        mat[0][0] = a;
        mat[1][1] = b;
        mat[2][2] = c;
    }

    @Deprecated
    public void setSacale(float a, float b, float c) {
        setScale(a, b, c);
    }

    public void setRotateX(float ang) {
        setIdentity();
        float rad = (float) Math.toRadians(ang);
        float s = (float) Math.sin(rad);
        float c = (float) Math.cos(rad);
        mat[1][1] = c;  mat[1][2] = -s;
        mat[2][1] = s;  mat[2][2] = c;
    }

    public void setRotateY(float ang) {
        setIdentity();
        float rad = (float) Math.toRadians(ang);
        float s = (float) Math.sin(rad);
        float c = (float) Math.cos(rad);
        mat[0][0] = c;   mat[0][2] = -s;
        mat[2][0] = s;   mat[2][2] = c;
    }

    public void setRotateZ(float ang) {
        setIdentity();
        float rad = (float) Math.toRadians(ang);
        float s = (float) Math.sin(rad);
        float c = (float) Math.cos(rad);
        mat[0][0] = c;  mat[0][1] = -s;
        mat[1][0] = s;  mat[1][1] = c;
    }
    /**
     * Rotação em torno de um eixo arbitrário (fórmula de Rodrigues).
     *
     * @param ang  ângulo em GRAUS
     * @param ux   componente X do eixo de rotação
     * @param uy   componente Y do eixo de rotação
     * @param uz   componente Z do eixo de rotação
     *
     * O eixo é normalizado internamente, então não precisa vir unitário.
     */
    public void setRotateAxis(float ang, float ux, float uy, float uz) {
        setIdentity();

        // Normaliza o eixo
        float len = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        if (len == 0) return; // eixo nulo -> matriz identidade
        ux /= len;
        uy /= len;
        uz /= len;

        float rad = (float) Math.toRadians(ang);
        float c = (float) Math.cos(rad);
        float s = (float) Math.sin(rad);
        float t = 1.0f - c;

        mat[0][0] = ux * ux * t + c;
        mat[0][1] = ux * uy * t - uz * s;
        mat[0][2] = ux * uz * t + uy * s;
        mat[0][3] = 0;

        mat[1][0] = uy * ux * t + uz * s;
        mat[1][1] = uy * uy * t + c;
        mat[1][2] = uy * uz * t - ux * s;
        mat[1][3] = 0;

        mat[2][0] = uz * ux * t - uy * s;
        mat[2][1] = uz * uy * t + ux * s;
        mat[2][2] = uz * uz * t + c;
        mat[2][3] = 0;

        mat[3][0] = 0;
        mat[3][1] = 0;
        mat[3][2] = 0;
        mat[3][3] = 1;
    }
    
    /**
     * Shearing genérico.
     *
     * Cada parâmetro indica quanto de um eixo é somado a outro.
     *   shx_y = quanto de Y é somado ao X     (x' = x + shx_y * y)
     *   shx_z = quanto de Z é somado ao X     (x' = x + shx_z * z)
     *   shy_x = quanto de X é somado ao Y     (y' = y + shy_x * x)
     *   shy_z = quanto de Z é somado ao Y     (y' = y + shy_z * z)
     *   shz_x = quanto de X é somado ao Z     (z' = z + shz_x * x)
     *   shz_y = quanto de Y é somado ao Z     (z' = z + shz_y * y)
     */
    public void setShear(float shx_y, float shx_z,
                         float shy_x, float shy_z,
                         float shz_x, float shz_y) {
        setIdentity();

        mat[0][1] = shx_y;
        mat[0][2] = shx_z;

        mat[1][0] = shy_x;
        mat[1][2] = shy_z;

        mat[2][0] = shz_x;
        mat[2][1] = shz_y;
    }

    // ---------------- PROJEÇÕES ----------------

    /**
     * Projeção Ortográfica (paralela pura, axonométrica).
     * Os eixos X e Y passam direto; Z é preservado (para uso em depth/ordenação),
     * mas não afeta a posição em tela.
     */
    public void setOrtographicProjection() {
        setIdentity();
    }

    /**
     * Compatibilidade: era chamada no MainCanvas original.
     */
    public void setParalelProjection() {
        setOrtographicProjection();
    }

    /**
     * Projeção Oblíqua.
     * @param alpha  fator de redução da profundidade (1.0 = Cavalier, 0.5 = Cabinet)
     * @param theta  ângulo em graus
     */
    public void setObliqueProjection(float alpha, float theta) {
        setIdentity();
        float rad = (float) Math.toRadians(theta);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        mat[0][0] = 1;
        mat[0][1] = 0;
        mat[0][2] = alpha * cos;   // X é deslocado por Z
        mat[0][3] = 0;

        mat[1][0] = 0;
        mat[1][1] = 1;
        mat[1][2] = alpha * sin;   // Y é deslocado por Z
        mat[1][3] = 0;

        mat[2][0] = 0;
        mat[2][1] = 0;
        mat[2][2] = 1;             // Z preservado
        mat[2][3] = 0;

        mat[3][0] = 0;
        mat[3][1] = 0;
        mat[3][2] = 0;
        mat[3][3] = 1;
    }

    /**
     * Atalhos típicos:
     *   Cavalier: alpha=1, theta=45
     *   Cabinet : alpha=0.5, theta=63.4
     */
    public void setCavalierProjection() {
        setObliqueProjection(1.0f, 45f);
    }

    public void setCabinetProjection() {
        setObliqueProjection(0.5f, 63.4f);
    }

    /**
     * Projeção em Perspectiva com 1 ponto de fuga.
     * @param d distância da câmera até o plano de projeção (em unidades de mundo).
     *          Valores maiores = menos distorção; menores = mais "olho de peixe".
     */
    public void setPerspectiveProjection(float d) {
        setIdentity();
        mat[3][2] = -1.0f / d;   // essa linha é a chave da divisão por w
    }

    // ---------------- OPERAÇÕES ----------------

    public Ponto3D multiplicaPonto(Ponto3D p) {
        float x1 = mat[0][0]*p.x + mat[0][1]*p.y + mat[0][2]*p.z + mat[0][3]*p.w;
        float y1 = mat[1][0]*p.x + mat[1][1]*p.y + mat[1][2]*p.z + mat[1][3]*p.w;
        float z1 = mat[2][0]*p.x + mat[2][1]*p.y + mat[2][2]*p.z + mat[2][3]*p.w;
        float w1 = mat[3][0]*p.x + mat[3][1]*p.y + mat[3][2]*p.z + mat[3][3]*p.w;

        if (w1 == 0) w1 = 1; // proteção contra divisão por zero
        return new Ponto3D(x1/w1, y1/w1, z1/w1, w1/w1);
    }

    public static Mat4x4 multiplicaMatrizes(Mat4x4 a, Mat4x4 b) {
        Mat4x4 r = new Mat4x4();
        r.zera();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++)
                    r.mat[i][j] += a.mat[i][k] * b.mat[k][j];
        return r;
    }
 
    public Mat4x4 multiplica(Mat4x4 outra) {
        return multiplicaMatrizes(this, outra);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%8.3f ", mat[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
}
