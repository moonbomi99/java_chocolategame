import java.net.URL;

public class MovingShape extends Shape{
	public MovingShape(URL imgURL, int x, int y, int margin, int steps, int xBoundary, int yBoundary) {
		// imgPath : �׸� ������ ��θ�
		// x, y : �̹����� ��ġ ��ǥ
		// margin : �� �̹����� ������ ��Ÿ���� ���� (�� �����ȿ� ������ �浹 �� ������ �Ǵ� �ϱ� ����)
		// steps : �̹����� �����϶� �̵��ϴ� ��ǥ ����
		// xBoundary, yBoundary : �׸��� �̵��� �� �ִ� ��ǥ�� �ִ밪
		super (imgURL, x, y, margin, steps, xBoundary, yBoundary);
	}
	
	public MovingShape(URL imgURL, int margin, int steps, int xBoundary, int yBoundary) {
		super (imgURL, margin, steps, xBoundary, yBoundary);
	}

	public void move() {
		y=y+steps;
	}
}
