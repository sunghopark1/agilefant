package fi.hut.soberit.agilefant.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.Assignment;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogHistoryEntry;
import fi.hut.soberit.agilefant.model.BacklogHourEntry;
import fi.hut.soberit.agilefant.model.Holiday;
import fi.hut.soberit.agilefant.model.HourEntry;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.Story;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.model.Team;
import fi.hut.soberit.agilefant.model.User;
import flexjson.JSON;


@SuppressWarnings("serial")
@Component("ajaxExceptionHandler")
@Scope("prototype")
public class AjaxExceptionHandler extends ActionSupport {

    private static final long serialVersionUID = -3087595845171961874L;

    private Exception exception;
    
    private String errorMessage;
    
    private String trace;
    
    // Exception results
    public static final String genericExceptionResult      = "genericException";
    public static final String constraintViolationResult   = "conflict";
    public static final String illegalArgumentResult       = "input";
    public static final String objectNotFoundResult        = "objectNotFound";
    public static final Map<Class<?>, String> typesToi18n = new HashMap<Class<?>, String>() {{
        put(Story.class, "story.notFound");
        put(Task.class, "task.notFound");
        put(Iteration.class, "iteration.notFound");
        put(Project.class, "project.notFound");
        put(Product.class, "product.notFound");
        put(Backlog.class, "backlog.notFound");
        put(User.class, "user.notFound");
        put(Team.class, "team.notFound");
        put(HourEntry.class, "hourEntry.notFound");
        put(Assignment.class, "assigment.notFound");
        put(BacklogHistoryEntry.class, "backlogHistoryEntry.notFound");
        put(BacklogHourEntry.class, "backlogHourEntry.notFound");
        put(Holiday.class, "holiday.notFound");
    }};
  
    

    public String handle() {
        if (exception instanceof ObjectNotFoundException) {
            return this.handleObjectNotFoundException((ObjectNotFoundException)exception);
        }
        trace = traceToString(exception);
        errorMessage = exception.getMessage();
        return genericExceptionResult;
    }
    
    public String handleObjectNotFoundException(ObjectNotFoundException onfe) {
        if(onfe.getI18nKey() != null) {
            this.errorMessage = this.getText(onfe.getI18nKey());
        } else {
            this.errorMessage = this.onfeToI18nString(onfe);
        }
        return objectNotFoundResult;
    }
    
    private String onfeToI18nString(ObjectNotFoundException onfe) {
        if(typesToi18n.containsKey(onfe.getTargetModel())) {
            return this.getText(typesToi18n.get(onfe.getTargetModel()));
        } else {
            return this.getText("unknown.notFound");
        }
    }
    
    private String traceToString(Exception exception) {
        StringWriter stackTrace = new StringWriter();
        PrintWriter pw = new PrintWriter(stackTrace);
        exception.printStackTrace(pw);
        return stackTrace.toString();
    }

    // AUTOGENERATED
    
    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
    
    @JSON
    public String getErrorMessage() {
        return errorMessage;
    }

    @JSON
    public String getTrace() {
        return trace;
    }

}
