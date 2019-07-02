package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.enterprise.inject.spi.BeanManager;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.apache.log4j.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContainerLifecycle;

import com.sun.net.httpserver.HttpServer;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.MainStage;
import de.machmireinebook.epubeditor.httpserver.EpubHttpHandler;
import de.machmireinebook.epubeditor.httpserver.ResourceHttpHandler;

public class EpubEditorApplication extends Application
{
    private static final Logger logger = Logger.getLogger(EpubEditorApplication.class);

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private MainStage mainStage;
    private static final int SPLASH_WIDTH = 276;
    private static final int SPLASH_HEIGHT = 276;
    private Image applicationIcon;
    private static ContainerLifecycle lifecycle = null;

    private EpubHttpHandler epubHttpHandler;
    private HttpServer server;

    @Override
    public void init()
    {
        ImageView splash = new ImageView(new Image(getClass().getResourceAsStream("/rocket.png")));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Starte epub4mmee ...");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        splash.setStyle("fx-margin: auto;");
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-padding: 5; -fx-background-color: white; -fx-border-width:5; -fx-border-color: #B3204D;");
        splashLayout.setEffect(new DropShadow());
        applicationIcon = new Image(getClass().getResourceAsStream("/rocket.png"));
    }

    @Override
    public void start(Stage initStage)
    {
        final Task<Boolean> initTask = new Task<>()
        {
            @Override
            protected Boolean call() throws Exception
            {
                updateProgress(0, 3);
                updateMessage("Initialization");
                EpubEditorConfiguration.initLogger();

                updateProgress(1, 3);
                updateMessage("Starting base components");
                lifecycle = WebBeansContext.currentInstance().getService(ContainerLifecycle.class);

                updateProgress(2, 3);
                updateMessage("Starting internal web server");
                server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8777), 1000);
                epubHttpHandler = new EpubHttpHandler();
                server.createContext("/", epubHttpHandler);

                ResourceHttpHandler resourceHttpHandler = new ResourceHttpHandler();
                server.createContext("/codemirror", resourceHttpHandler);
                server.createContext("/dtd", resourceHttpHandler);
                server.createContext("/modes", resourceHttpHandler);
                server.createContext("/images", resourceHttpHandler);

                server.start();

                updateProgress(3, 3);
                updateMessage("Scripto is starting");

                return true;
            }
        };

        showSplash(initStage, initTask);
        new Thread(initTask).start();
    }

    private void createMainStage()
    {
        try
        {
            //erst hier da inittask in einem eigenen thread läuft was dazu führt dass die contexte nicht initialisiert sind
            lifecycle.startApplication(this);
            BeanManager beanManager = lifecycle.getBeanManager();
            /*BeanFactory beanFactory = */ new BeanFactory(beanManager);

            mainStage = BeanFactory.getInstance().getBean(MainStage.class);
            mainStage.init(StageStyle.DECORATED, applicationIcon);
            EpubEditorConfiguration configuration =  BeanFactory.getInstance().getBean(EpubEditorConfiguration.class);
            configuration.setMainWindow(mainStage);
            setUserAgentStylesheet(STYLESHEET_MODENA);

            mainStage.setOnShown(event ->
            {
                configuration.readConfiguration();
                MainController controller =  BeanFactory.getInstance().getBean(MainController.class);
                controller.setStage(mainStage);
                controller.newMinimalEpubAction();
                controller.setEpubHttpHandler(epubHttpHandler);
            });

        }
        catch (IOException e)
        {
            logger.error("", e);
            new org.controlsfx.dialog.ExceptionDialog(e).showAndWait();
        }
    }

    private void showSplash(final Stage initStage, final Task<Boolean> task)
    {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED)
            {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> {
                    initStage.hide();
                    createMainStage();
                    mainStage.show();
                });
                fadeSplash.play();
            }
            else if (newState == Worker.State.FAILED)
            {
                initStage.hide();
                Throwable t = task.getException();
                logger.error("", t);
                ExceptionDialog.showAndWait(t, mainStage,  "Can't load application", "Kann Applikation nicht laden, bitte Fehlermeldung weitergeben");
            }
        });

        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2.0);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2.0);
        initStage.getIcons().add(applicationIcon);
        initStage.setTitle("Scripto");
        initStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
        if (mainStage != null)
        {
            EpubEditorConfiguration configuration = BeanFactory.getInstance().getBean(EpubEditorConfiguration.class);
            configuration.saveConfiguration();
        }
        if (server != null)
        {
            server.stop(0);
        }
    }
}
