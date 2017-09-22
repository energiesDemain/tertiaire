package com.ed.cgdd.launcher;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ed.cgdd.derby.loadparam.LoadParamService;
import com.ed.cgdd.derby.model.progression.Progression;
import com.ed.cgdd.derby.model.progression.ProgressionStep;
import com.ed.cgdd.derby.process.ProcessService;

public class LauncherProcess {

	private final static Logger LOG = LogManager.getLogger(LauncherProcess.class);

	public static void main(String[] args) throws SQLException, IOException {
		LOG.info("Start engine");

		Progression progression = new Progression();
		progression.setStep(ProgressionStep.INITIALISATION);
		frame(progression);

		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextProcess.xml");
		LoadParamService serviceParam = (LoadParamService) context.getBean("paramService");

		// recuperation des parametres d'entrees dans le parc

		serviceParam.initParam(progression);

		// lancement des methodes de calcul

		ProcessService service = (ProcessService) context.getBean("processService");

		service.process(progression);
		progression.setStep(ProgressionStep.FIN);

		LOG.info("End engine");

	}

	private static void frame(final Progression progression) {
		final JFrame frame = new JFrame("CGDD");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(350, 200);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getWidth() / 2, dim.height / 2 - frame.getHeight() / 2);

		GridBagLayout layout = new GridBagLayout();
		frame.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();

		JLabel labelTitle = new JLabel("Bienvenue !", SwingConstants.CENTER);
		constraints.anchor = GridBagConstraints.PAGE_START;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.ipady = 30;
		frame.add(labelTitle, constraints);

		JLabel labelStep = new JLabel("Etape : ", SwingConstants.CENTER);
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.ipady = 5;
		frame.add(labelStep, constraints);

		final JLabel step = new JLabel(progression.getStep().name(), SwingConstants.CENTER);
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		frame.add(step, constraints);

		final JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		frame.add(progressBar, constraints);

		PropertyChangeListener listener = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("handledParc")) {
					int val = progression.getHandledParc() * 100 / progression.getParcSize();
					progressBar.setValue(val);
					progressBar.repaint();
					frame.repaint();
				} else if (evt.getPropertyName().equals("step")) {
					step.setText(progression.getStep().name());
					frame.repaint();
				}
			}
		};

		progression.addPropertyChangeListener(listener);
		frame.setVisible(true);
	}
}
