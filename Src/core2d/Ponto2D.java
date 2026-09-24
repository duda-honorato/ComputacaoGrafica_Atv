package core2d;
//Diz que essa classe pertetece ao pct core2d
//Ela pode ser usada por classes do memso pct ou
//por quem importar cored2d.Pontos2D

public class Ponto2D {
	float X;
	float Y;
	//coordenadas do ponto no plano cartesiano
	
	public Ponto2D(float x, float y) {
		super();
		X = x;
		Y = y;
	}
	//Construtor
	
	public void translate(float x,float y) {
		X = X+x;
		Y = Y+y;
	//Move o ponto somando um deslocamento (x, y)
	//Ex: Ponto2D p = new Ponto2D(10, 20); p.translate(5, -3); → agora p está em (15, 17)
	//Não retorna nada (void) — modifica o próprio objeto (in-place)
	}
	public void scale(float x,float y) {
		X = X*x;
		Y = Y*y;
	//Multiplica cada coordenada por um fator
	//Exemplo: p.scale(2, 2) dobra a distância do ponto até a origem
	}
	public void rotate(float ang) {
		float nX = (float)(X*Math.cos(ang)+Y*Math.sin(ang));
		float nY = (float)(-X*Math.sin(ang)+Y*Math.cos(ang));
		
		X = nX;
		Y = nY;
		
	}
}
