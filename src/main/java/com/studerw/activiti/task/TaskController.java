package com.studerw.activiti.task;

import com.studerw.activiti.model.Response;
import com.studerw.activiti.model.task.TaskApprovalForm;
import com.studerw.activiti.model.task.AssignedTask;
import com.studerw.activiti.web.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @author William Studer
 * Date: 5/18/14
 */
@Controller
public class TaskController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

    @Autowired protected LocalTaskService taskService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/tasks.htm", method = RequestMethod.GET)
    public String index(ModelMap model, HttpServletRequest request) {
        List<AssignedTask> tasks = this.taskService.getTasks(currentUserName());
        model.addAttribute("tasks", tasks);

        return "tasks";
    }

    @RequestMapping(value = "/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<List<AssignedTask>>> getTasks(
            HttpServletRequest request) {
        String userName = currentUserName();
        LOG.debug("TaskController: tasks for user: " + userName);
        List<AssignedTask> tasks = taskService.getTasks(userName);
        LOG.debug("returning json response of: " + tasks.size() + " for user : " + userName);
        Response res = new Response(true, "tasks for user: " + userName, tasks);
        return new ResponseEntity<Response<List<AssignedTask>>>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/tasks/approve.htm", method = RequestMethod.POST)
    public String approve(@Valid @ModelAttribute TaskApprovalForm taskApprovalForm,
                          BindingResult result,
                          final RedirectAttributes redirectAttributes){
        LOG.debug("task approval: {}", taskApprovalForm.toString());

        if (result.hasFieldErrors()) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("errors", result.getFieldErrors());
            return "redirect:/tasks.htm";
        }

        this.taskService.approveOrRejectDoc(taskApprovalForm);

        redirectAttributes.addFlashAttribute("msg", "The task has been completed.");
        return "redirect:/tasks.htm";
    }


}
