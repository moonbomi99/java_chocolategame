import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;


public class AvoidingChocolate {
	JFrame frame=new JFrame();				// ��ü GUI�� ���� �����ӿ� ���� ���۷���
	private final int C_MARGIN = 50;  		
	private final int T_MARGIN = 80;  		
	private final int WIDTH = 580; 		
	private final int HEIGHT = 600; 
	private final int CHOCOLATE_INTERVAL= 1;	// ���ݸ��� ��Ÿ���� �ֱ�
	private final int TOOTHPASTE_INTERVAL= 20;	// ġ���� ��Ÿ���� �ֱ�
	private final int SPEED = 50;			// �ִϸ��̼��� �ӵ� (�и���)
	private final int STEP =10;			// �׸� ��ü���� �ѹ��� �����̴� �Ƚ� ��
	// ��ư ����� ���� ��Ʈ ���꿡 ���� �����
	private final int START = 1;
	private final int SUSPEND = 2;
	private final int CONT = 4;
	private final int END = 8;

	private final String CHOCOLATE_PIC = "/pic/chocolate.jpg";
	private final String TOOTHPASTE_PIC = "/pic/toothpaste.jpg";
	private final String PLAYER_PIC = "/pic/mainc.jpg";
	private final String MAIN_PIC = "/pic/main3.png";
	private final String VIRUS_PIC="/pic/virus.jpg";
	private final String START_SOUND = "/pic/start7.wav";
	private final String BOOM_SOUND = "/pic/boom.wav";
	private final String PLUS_SOUND="/pic/plus.wav";
	private final String FINISH_SOUND="/pic/finish.wav";
	int time=0;
	int heart=3;
	int gamePanelWidth, gamePanelHeight;	// ���� ������ �̷���� ������ ũ�� 
	JPanel controlPanel=new JPanel();		
	JButton start=new JButton("����");		
	JButton end=new JButton("����");			
	JButton suspend=new JButton("�Ͻ�����");
	JButton cont=new JButton("���");		
	JLabel heartlabel=new JLabel("�� : "+heart);
	JLabel timelabel=new JLabel("�ð�  : "+time+"��");
	JLayeredPane lp = new JLayeredPane();	// ȭ���� ������ ��ġ�� ���� Panel ���̾�
	JPanel coverPanel;						
	GamePanel gamePanel;					
	Timer goAnime;							// �׷��� ��ü�� �������� �����ϱ� ���� Ÿ�̸�
	Timer goClock;							// �ð豸���� ���� ���� Ÿ�̸�
	ClockListener clockListener;			// �ð踦 �����ϱ� ���� ������
	ArrayList<Shape> chocolateList;			
	ArrayList<Shape> toothpasteList;		
	Shape player;							
	DirectionListener keyListener;			// ȭ��ǥ �������� �����ϴ� ������
	private AudioClip backgroundSound;		// ���� ��� ����
	private AudioClip boomSound;			// ���ݸ� �浹����
	private AudioClip plusSound;            // ġ�� �浹����  
	private AudioClip finishSound;          // ���� ������ �� ����
	static String playerName;				
	public static void main(String [] args) {
		playerName=JOptionPane.showInputDialog("�̸��� �Է����ּ��� :");	
		new AvoidingChocolate().go();									
	}

	public void go() {

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		controlPanel.add(start);
		controlPanel.add(suspend);
		controlPanel.add(cont);
		controlPanel.add(end);
		controlPanel.add(timelabel);
		controlPanel.add(heartlabel);
		controlPanel.add(new JLabel(" Player : "));
		controlPanel.add(new JLabel(playerName));

		// ������ ������ ���÷��� �� �г�
		gamePanel = new GamePanel();
		gamePanel.setBounds(0,0,WIDTH,HEIGHT);

		// �ʱ�ȭ���� ���� �г�
		coverPanel = new CoverPanel();
		coverPanel.setBounds(0,0,WIDTH,HEIGHT);

		lp.add(gamePanel, new Integer(0));
		lp.add(coverPanel, new Integer(1));

		frame.add(lp);
		frame.add(BorderLayout.CENTER, lp);
		frame.add(BorderLayout.SOUTH, controlPanel);

		gamePanelWidth = gamePanel.getWidth() -70;
		gamePanelHeight = gamePanel.getHeight() -140;

		prepareFalling();

		clockListener = new ClockListener();
		goClock = new Timer(1000, clockListener);			// �ð��� �ʴ����� ��Ÿ���� ���� ������
		goAnime = new Timer(SPEED, new AnimeListener());	// �׸��� �̵��� ó���ϱ� ���� ������

		gamePanel.addKeyListener(new DirectionListener());	// Ű���� ������ ��ġ
		gamePanel.setFocusable(false);						// �ʱ⿡�� ��Ű�� �ȵǰ� ��(�� Ű �ȸ���)

		start.addActionListener(new StartListener());
		suspend.addActionListener(new SuspendListener());
		cont.addActionListener(new ContListener());
		end.addActionListener(new EndListener());

		// ������ ���� ���� ���� ��ġ
		try {
			// backgroundSound = JApplet.newAudioClip(new URL("file", "localhost","/res/start.wav"));
			// boomSound = JApplet.newAudioClip(new URL("file", "localhost","/res/boom.wav"));
			// ���� ����� ����θ� ��Ÿ���� ���ϴ� ����̾, jar���Ϸ� �������� ���鶧 ��θ� ã�� ���ϴ�
			// ������ ����. ���� getClass()�� ����Ͽ� ������� URL�� ���ϴ� ����� �Ʒ�ó�� ����ؾ� ��
			// ���⿡�� root�� �Ǵ� ������ ���� �� ���α׷��� ����Ǵ� ���̴� ���� ������ �־��־�� ��
			backgroundSound = JApplet.newAudioClip(getClass().getResource(START_SOUND));
			boomSound = JApplet.newAudioClip(getClass().getResource(BOOM_SOUND));
			plusSound=JApplet.newAudioClip(getClass().getResource(PLUS_SOUND));
			finishSound=JApplet.newAudioClip(getClass().getResource(FINISH_SOUND));

		}
		catch(Exception e){
			System.out.println("���� ���� �ε� ����");
		}

		buttonToggler(START+END);	
		frame.setSize(WIDTH,HEIGHT);
		frame.setVisible(true);
	}

	// ��ư�� Ȱ�� ��Ȱ��ȭ�� ���� ��ƾ
	private void buttonToggler(int flags) {
		if ((flags & START) != 0)
			start.setEnabled(true);
		else
			start.setEnabled(false);
		if ((flags & SUSPEND) != 0)
			suspend.setEnabled(true);
		else
			suspend.setEnabled(false);
		if ((flags & CONT) != 0)
			cont.setEnabled(true);
		else
			cont.setEnabled(false);
		if ((flags & END) != 0)
			end.setEnabled(true);
		else
			end.setEnabled(false);
	}

	private void prepareFalling() {
		toothpasteList = new ArrayList<Shape>();		// ġ���� ����Ʈ�� ���
		chocolateList = new ArrayList<Shape>();			

		chocolateList.add(new MovingShape(getClass().getResource(CHOCOLATE_PIC), C_MARGIN, (int) (Math.random() * 10)+5, gamePanelWidth, gamePanelHeight));
	}


	private void finishGame() {
		player = new Shape(getClass().getResource(VIRUS_PIC), C_MARGIN, gamePanelWidth, gamePanelHeight/2);
		chocolateList=new ArrayList<Shape>();
		toothpasteList=new ArrayList<Shape>();
		finishSound.play();
		backgroundSound.stop();				
		goClock.stop();						
		goAnime.stop();						
		gamePanel.setFocusable(false);		// ��Ŀ�� �ȵǰ� ��(�� Ű �ȸ���)
		buttonToggler(START+END);				// Ȱ��ȭ ��ư�� ����
		frame.repaint();
	}


	private Shape getChocolate(String pic, int margin, int steps) {
		Shape newChocolate;
		newChocolate =  new MovingShape(getClass().getResource(pic), margin, steps, gamePanelWidth, gamePanelHeight);
		return newChocolate;
	}

	private Shape getToothpaste(String pic,int margin,int steps) {
		Shape newToothpaste;
		newToothpaste=new MovingShape(getClass().getResource(pic), margin, steps, gamePanelWidth, gamePanelHeight);
		return newToothpaste;
	}

	// goAnime Ÿ�̸ӿ� ���� �ֱ������� ����� ����
	// ��ü�� ������, �浹�� ���� ����
	public class AnimeListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			for (Shape s : chocolateList) {
				if (s.collide(new Point(player.x, player.y))) {
					chocolateList.remove(s);
					boomSound.play();	
					heart--;
					heartlabel.setText("�� : "+heart);
					if(heart==0) {
						finishGame();		
					}
					return;
				}
			}
			for (Shape s : toothpasteList) {
				if (s.collide(new Point(player.x, player.y))) {
					plusSound.play();
					toothpasteList.remove(s);
					if(heart<3)
					{
						heart++;
						heartlabel.setText("�� : "+heart);
					}
					return;
				}
			}
			// �׸� ��ü���� �̵���Ŵ
			for (Shape s : chocolateList) {
				s.move();
			}
			for (Shape s : toothpasteList) {
				s.move();
			}

			frame.repaint();								
		}
	}

	// ���� ��ư�� ��û��
	class StartListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			lp.setLayer(gamePanel, 2);						// gamePanel �� ������ ������ ��
			gamePanel.setFocusable(true);					// gamePanel�� ��Ŀ�̵� �� �ְ� ��
			gamePanel.requestFocus();						// ��Ŀ���� ������(�̰� �ݵ�� �ʿ�)

			player = new Shape(getClass().getResource(PLAYER_PIC), C_MARGIN, gamePanelWidth, gamePanelHeight);
			
			backgroundSound.play();	
			
			goAnime.start();								// �׸���ü �������� ���� ����

			clockListener.reset();				
			heartlabel.setText("�� : "+heart);
			timelabel.setText("�ð�  : "+time+"��");	
			goClock.start();								



			prepareFalling();								

			buttonToggler(SUSPEND+END);						

		}
	}

	class SuspendListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			goClock.stop();		
			goAnime.stop();
			gamePanel.setFocusable(false);					// ���� �����ӿ� Ű �ȸ԰� ��
			buttonToggler(CONT+END);						// Ȱ��ȭ ��ư�� ����
		}
	}

	class ContListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			goClock.restart();
			goAnime.restart();
			gamePanel.setFocusable(true);					// ���� ������ Ű �԰� ��
			gamePanel.requestFocus();						// ��ü �����ֿ� ��Ŀ���ؼ� Ű �԰� ��
			buttonToggler(SUSPEND+END);						// Ȱ��ȭ ��ư�� ����
		}
	}

	class EndListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	class GamePanel extends JPanel {
		public void paintComponent(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0,0,this.getWidth(), this.getHeight());		// ȭ�� �����


			// ���ӿ� ���Ǵ� �׷��� ��ü�� ��� �׷���
			for (Shape s : chocolateList) {
				s.draw(g, this);
			}
			for (Shape s : toothpasteList) {
				s.draw(g, this);
			}
			player.draw(g, this);	
		}
	}

	class CoverPanel extends JPanel {
		public void paintComponent(Graphics g) {
			Image image = new ImageIcon(getClass().getResource(MAIN_PIC)).getImage(); 
			g.drawImage(image,0,0,this);
		}
	}

	private class ClockListener implements ActionListener {

		public void actionPerformed (ActionEvent event) {		
			time++;						
			timelabel.setText("�ð�  : "+time+"��");

			if (time % CHOCOLATE_INTERVAL == 0)
				chocolateList.add(getChocolate(CHOCOLATE_PIC, C_MARGIN, (int) (Math.random() * 10)+5));

			if (time % TOOTHPASTE_INTERVAL == 0) 
				toothpasteList.add(getToothpaste(TOOTHPASTE_PIC, T_MARGIN, STEP));
			
			if(time%31==0) {
				backgroundSound.stop();
				backgroundSound.play();
			}
		}

		public void reset() {
			time = 0;
			heart=3;
		}
		public int getElaspedTime() {
			return time;
		}
	}


	class DirectionListener implements KeyListener {
		public void keyPressed (KeyEvent event) {
			switch (event.getKeyCode()){
			case KeyEvent.VK_LEFT:
				if (player.x >= 0)
					player.x -= STEP;
				break;
			case KeyEvent.VK_RIGHT:
				if (player.x <= gamePanelWidth)
					player.x += STEP;
				break;
			}
		}
		public void keyTyped (KeyEvent event) {}
		public void keyReleased (KeyEvent event) {}
	}
}