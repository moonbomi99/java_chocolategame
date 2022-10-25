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
	JFrame frame=new JFrame();				// 전체 GUI를 담을 프레임에 대한 레퍼런스
	private final int C_MARGIN = 50;  		
	private final int T_MARGIN = 80;  		
	private final int WIDTH = 580; 		
	private final int HEIGHT = 600; 
	private final int CHOCOLATE_INTERVAL= 1;	// 초콜릿이 나타나는 주기
	private final int TOOTHPASTE_INTERVAL= 20;	// 치약이 나타나는 주기
	private final int SPEED = 50;			// 애니매이션의 속도 (밀리초)
	private final int STEP =10;			// 그림 객체들이 한번에 움직이는 픽슬 수
	// 버튼 토글을 위한 비트 연산에 사용될 상수들
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
	int gamePanelWidth, gamePanelHeight;	// 실제 게임이 이루어질 영역의 크기 
	JPanel controlPanel=new JPanel();		
	JButton start=new JButton("시작");		
	JButton end=new JButton("종료");			
	JButton suspend=new JButton("일시중지");
	JButton cont=new JButton("계속");		
	JLabel heartlabel=new JLabel("♥ : "+heart);
	JLabel timelabel=new JLabel("시간  : "+time+"초");
	JLayeredPane lp = new JLayeredPane();	// 화면을 여러장 겹치기 위한 Panel 레이어
	JPanel coverPanel;						
	GamePanel gamePanel;					
	Timer goAnime;							// 그래픽 객체의 움직임을 관장하기 위한 타이머
	Timer goClock;							// 시계구현을 위한 위한 타이머
	ClockListener clockListener;			// 시계를 구현하기 위한 리스너
	ArrayList<Shape> chocolateList;			
	ArrayList<Shape> toothpasteList;		
	Shape player;							
	DirectionListener keyListener;			// 화살표 움직임을 감지하는 리스너
	private AudioClip backgroundSound;		// 게임 배경 음악
	private AudioClip boomSound;			// 초콜릿 충돌음향
	private AudioClip plusSound;            // 치약 충돌음향  
	private AudioClip finishSound;          // 게임 끝났을 때 음악
	static String playerName;				
	public static void main(String [] args) {
		playerName=JOptionPane.showInputDialog("이름을 입력해주세요 :");	
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

		// 게임의 진행이 디스플레이 될 패널
		gamePanel = new GamePanel();
		gamePanel.setBounds(0,0,WIDTH,HEIGHT);

		// 초기화면을 위한 패널
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
		goClock = new Timer(1000, clockListener);			// 시간을 초단위로 나타내기 위한 리스너
		goAnime = new Timer(SPEED, new AnimeListener());	// 그림의 이동을 처리하기 위한 리스너

		gamePanel.addKeyListener(new DirectionListener());	// 키보드 리스너 설치
		gamePanel.setFocusable(false);						// 초기에는 포키싱 안되게 함(즉 키 안먹음)

		start.addActionListener(new StartListener());
		suspend.addActionListener(new SuspendListener());
		cont.addActionListener(new ContListener());
		end.addActionListener(new EndListener());

		// 게임을 위한 음향 파일 설치
		try {
			// backgroundSound = JApplet.newAudioClip(new URL("file", "localhost","/res/start.wav"));
			// boomSound = JApplet.newAudioClip(new URL("file", "localhost","/res/boom.wav"));
			// 위의 방법은 상대경로를 나타내지 못하는 방법이어서, jar파일로 배포판을 만들때 경로를 찾지 못하는
			// 문제가 생김. 따라서 getClass()를 사용하여 상대적인 URL을 구하는 방법을 아래처럼 사용해야 함
			// 여기에서 root가 되는 폴더는 현재 이 프로그램이 수행되는 곳이니 같은 레벨에 넣어주어야 함
			backgroundSound = JApplet.newAudioClip(getClass().getResource(START_SOUND));
			boomSound = JApplet.newAudioClip(getClass().getResource(BOOM_SOUND));
			plusSound=JApplet.newAudioClip(getClass().getResource(PLUS_SOUND));
			finishSound=JApplet.newAudioClip(getClass().getResource(FINISH_SOUND));

		}
		catch(Exception e){
			System.out.println("음향 파일 로딩 실패");
		}

		buttonToggler(START+END);	
		frame.setSize(WIDTH,HEIGHT);
		frame.setVisible(true);
	}

	// 버튼의 활성 비활성화를 위한 루틴
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
		toothpasteList = new ArrayList<Shape>();		// 치약의 리스트는 비움
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
		gamePanel.setFocusable(false);		// 포커싱 안되게 함(즉 키 안먹음)
		buttonToggler(START+END);				// 활성화 버튼의 조정
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

	// goAnime 타이머에 의해 주기적으로 실행될 내용
	// 객체의 움직임, 충돌의 논리를 구현
	public class AnimeListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			for (Shape s : chocolateList) {
				if (s.collide(new Point(player.x, player.y))) {
					chocolateList.remove(s);
					boomSound.play();	
					heart--;
					heartlabel.setText("♥ : "+heart);
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
						heartlabel.setText("♥ : "+heart);
					}
					return;
				}
			}
			// 그림 객체들을 이동시킴
			for (Shape s : chocolateList) {
				s.move();
			}
			for (Shape s : toothpasteList) {
				s.move();
			}

			frame.repaint();								
		}
	}

	// 시작 버튼의 감청자
	class StartListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			lp.setLayer(gamePanel, 2);						// gamePanel 이 앞으로 나오게 함
			gamePanel.setFocusable(true);					// gamePanel이 포커싱될 수 있게 함
			gamePanel.requestFocus();						// 포커싱을 맞춰줌(이것 반드시 필요)

			player = new Shape(getClass().getResource(PLAYER_PIC), C_MARGIN, gamePanelWidth, gamePanelHeight);
			
			backgroundSound.play();	
			
			goAnime.start();								// 그림객체 움직임을 위한 시작

			clockListener.reset();				
			heartlabel.setText("♥ : "+heart);
			timelabel.setText("시간  : "+time+"초");	
			goClock.start();								



			prepareFalling();								

			buttonToggler(SUSPEND+END);						

		}
	}

	class SuspendListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			goClock.stop();		
			goAnime.stop();
			gamePanel.setFocusable(false);					// 게임 프레임에 키 안먹게 함
			buttonToggler(CONT+END);						// 활성화 버튼의 조정
		}
	}

	class ContListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			goClock.restart();
			goAnime.restart();
			gamePanel.setFocusable(true);					// 게임 프레임 키 먹게 함
			gamePanel.requestFocus();						// 전체 프레밍에 포커싱해서 키 먹게 함
			buttonToggler(SUSPEND+END);						// 활성화 버튼의 조정
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
			g.fillRect(0,0,this.getWidth(), this.getHeight());		// 화면 지우기


			// 게임에 사용되는 그래픽 객체들 모두 그려줌
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
			timelabel.setText("시간  : "+time+"초");

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