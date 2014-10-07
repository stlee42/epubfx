package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.enterprise.inject.spi.BeanManager;

import de.machmireinebook.commons.cdi.BeanFactory;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.httpserver.EpubHttpHandler;
import de.machmireinebook.epubeditor.httpserver.ResourceHttpHandler;

import com.sun.net.httpserver.HttpServer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
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
import org.controlsfx.dialog.Dialogs;


public class EpubEditorApplication extends Application
{
    Logger logger = Logger.getLogger(EpubEditorApplication.class);

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
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

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage initStage)
    {
        final Task<Boolean> initTask = new Task<Boolean>()
        {

            @Override
            protected Boolean call() throws Exception
            {
                updateProgress(0, 3);
                updateMessage("Verbinde mit Datenbank");
                EpubEditorConfiguration.getInstance().init();

                updateProgress(1, 3);
                updateMessage("Starte Basiskomponenten");
                lifecycle = WebBeansContext.currentInstance().getService(ContainerLifecycle.class);
                lifecycle.startApplication(this);

                BeanManager beanManager = lifecycle.getBeanManager();
                /*BeanFactory beanFactory = */
                BeanFactory.initialize(beanManager);

                updateProgress(2, 3);
                updateMessage("Interner Server wird gestarte");
                server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8777), 1000);
                epubHttpHandler = new EpubHttpHandler();
                server.createContext("/", epubHttpHandler);

                ResourceHttpHandler resourceHttpHandler = new ResourceHttpHandler();
                server.createContext("/codemirror-4.4", resourceHttpHandler);
                server.createContext("/dtd", resourceHttpHandler);

                server.start();

                updateProgress(3, 3);
                updateMessage("epub4mmee wird gestartet");


                return true;
            }
        };

        try
        {
            showSplash(initStage, initTask);
            new Thread(initTask).start();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }

    private void createMainStage()
    {
            mainStage = new Stage(StageStyle.DECORATED);
            EpubEditorConfiguration.getInstance().setMainWindow(mainStage);

            Method staticMethod;
            final EpubEditorMainController controller;
            Class<EpubEditorMainController> controllerClass = EpubEditorMainController.class;
            try
            {
                staticMethod = EpubEditorMainController.class.getMethod("getInstance");
            }
            catch (NoSuchMethodException e)
            {
                logger.error("", e);
                Dialogs.create().showException(e);
                return;
            }

            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"), null, new JavaFXBuilderFactory(),
                        type -> BeanFactory.getInstance().getBean(type));
                Pane root = loader.load();
                Scene scene = new Scene(root);
                if (getClass().getResource("/application.css") != null)
                {
                    scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
                }

                setUserAgentStylesheet(STYLESHEET_MODENA);
                mainStage.setScene(scene);
                //set icon of the application
                mainStage.getIcons().add(applicationIcon);
                mainStage.setTitle("epub4mmee");
            }
            catch (IOException e)
            {
                logger.error("", e);
                Dialogs.create().showException(e);
            }
            try
            {
                controller = (EpubEditorMainController) staticMethod.invoke(controllerClass);
                controller.setStage(mainStage);
                controller.setEpubHttpHandler(epubHttpHandler);
                mainStage.setOnShown(event -> controller.newMinimalEpubAction());
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                logger.error("", e);
                Dialogs.create().showException(e);
            }
    }

    private void showSplash(final Stage initStage, final Task<Boolean> task) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED)
                {
                    loadProgress.progressProperty().unbind();
                    loadProgress.setProgress(1);
                    initStage.toFront();
                    FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                    fadeSplash.setFromValue(1.0);
                    fadeSplash.setToValue(0.0);
                    fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            initStage.hide();
                            createMainStage();
                            mainStage.show();
                        }
                    });
                    fadeSplash.play();
                }
                else if (newState == Worker.State.FAILED)
                {
                    initStage.hide();
                    Throwable t = task.getException();
                    Dialogs.create()
                            .owner(mainStage)
                            .title("Kann Applikation nicht laden")
                            .message("Kann Applikation nicht laden, bitte Fehlermeldung weitergeben")
                            .showException(t);
                }
            }
        });

        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.getIcons().add(applicationIcon);
        initStage.setTitle("epub4mmee");
        initStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
        if (mainStage != null)
        {
            EpubEditorConfiguration.getInstance().saveConfiguration();
        }
        if (server != null)
        {
            server.stop(0);
        }
    }
}
