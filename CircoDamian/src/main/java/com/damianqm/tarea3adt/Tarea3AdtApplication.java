package com.damianqm.tarea3adt;

import com.damianqm.tarea3adt.config.StageManager;

import com.damianqm.tarea3adt.view.FxmlView;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
		MongoRepositoriesAutoConfiguration.class })
public class Tarea3AdtApplication extends Application {

	private ConfigurableApplicationContext springContext;
	private StageManager stageManager;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void init() {
		String[] args = getParameters().getRaw().toArray(new String[0]);
		springContext = new SpringApplicationBuilder(Tarea3AdtApplication.class).run(args);
	}

	@Override
	public void start(Stage primaryStage) {
		stageManager = springContext.getBean(StageManager.class, primaryStage);
		stageManager.switchScene(FxmlView.BIENVENIDA);
	}

	@Override
	public void stop() {
		springContext.close();
	}
}
