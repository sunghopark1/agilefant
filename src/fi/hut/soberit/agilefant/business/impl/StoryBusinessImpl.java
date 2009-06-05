package fi.hut.soberit.agilefant.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.hut.soberit.agilefant.business.StoryBusiness;
import fi.hut.soberit.agilefant.db.BacklogDAO;
import fi.hut.soberit.agilefant.db.IterationDAO;
import fi.hut.soberit.agilefant.db.StoryDAO;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.State;
import fi.hut.soberit.agilefant.model.Story;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.security.SecurityUtil;
import fi.hut.soberit.agilefant.util.ResponsibleContainer;
import fi.hut.soberit.agilefant.util.StoryMetrics;

@Service("storyBusiness")
@Transactional
public class StoryBusinessImpl extends GenericBusinessImpl<Story> implements
        StoryBusiness {

    private StoryDAO storyDAO;
    @Autowired
    private BacklogDAO backlogDAO;
    @Autowired
    private IterationDAO iterationDAO;
    @Autowired
    private UserDAO userDAO;

    @Autowired
    public void setStoryDAO(StoryDAO storyDAO) {
        this.genericDAO = storyDAO;
        this.storyDAO = storyDAO;
    }

    public List<Story> getStoriesByBacklog(Backlog backlog) {
        return storyDAO.getStoriesByBacklog(backlog);
    }

    @Transactional(readOnly = true)
    public Collection<Task> getStoryContents(Story story, Iteration iteration) {
        // TODO Auto-generated method stub
        return null;
    }

    // public Collection<BacklogItem> getIterationGoalContents(int
    // iterationGoalId, int iterationId) {
    // IterationGoal goal = iterationGoalDAO.get(iterationGoalId);
    // Iteration iter = iterationDAO.get(iterationId);
    // if(iter == null) {
    // return null;
    // }
    // return getIterationGoalContents(goal, iter);
    // }

    // public Collection<BacklogItem> getIterationGoalContents(IterationGoal
    // goal,
    // Iteration iter) {
    //
    // List<BacklogItem> blis = backlogItemBusiness
    // .getBacklogItemsByBacklogWithCache(iter);
    // Collection<BacklogItem> goalBlis = new ArrayList<BacklogItem>();
    // for (BacklogItem bli : blis) {
    // if (bli.getIterationGoal() == goal) {
    // goalBlis.add(bli);
    // }
    // }
    // return goalBlis;
    // }

    @Transactional(readOnly = true)
    public Collection<ResponsibleContainer> getStoryResponsibles(Story story) {
        Collection<ResponsibleContainer> responsibleContainers = new ArrayList<ResponsibleContainer>();
        Collection<User> storyResponsibles = story.getResponsibles();
        for (User user : storyResponsibles) {
            responsibleContainers.add(new ResponsibleContainer(user, true));
        }
        return responsibleContainers;
    }

    public Story store(int storyId, int backlogId, Story dataItem,
            Set<Integer> responsibles) throws ObjectNotFoundException {
        Story item = null;
        if (storyId > 0) {
            item = storyDAO.get(storyId);
            if (item == null) {
                item = new Story();
            }
        }
        Backlog backlog = backlogDAO.get(backlogId);
        if (backlog == null) {
            throw new ObjectNotFoundException("backlog.notFound");
        }

        Set<User> responsibleUsers = new HashSet<User>();

        for (int userId : responsibles) {
            User responsible = userDAO.get(userId);
            if (responsible != null) {
                responsibleUsers.add(responsible);
            }
        }

        return this.store(item, backlog, dataItem, responsibleUsers);
    }

    public Story store(Story storable, Backlog backlog, Story dataItem,
            Set<User> responsibles) {

        boolean historyUpdated = false;

        if (backlog == null) {
            throw new IllegalArgumentException("Backlog must not be null.");
        }
        if (dataItem == null) {
            throw new IllegalArgumentException("No data given.");
        }
        if (storable == null) {
            storable = new Story();
            // storable.setCreatedDate(Calendar.getInstance().getTime());
            try {
                storable.setCreator(SecurityUtil.getLoggedUser()); // may fail
                                                                   // if request
                                                                   // is
                                                                   // multithreaded
            } catch (Exception e) {
            } // however, saving item should not fail.
        }
        storable.setDescription(dataItem.getDescription());
        // storable.setEffortLeft(dataItem.getEffortLeft());
        storable.setName(dataItem.getName());
        // if(storable.getOriginalEstimate() == null) {
        // if(dataItem.getOriginalEstimate() == null) {
        // storable.setOriginalEstimate(dataItem.getEffortLeft());
        // } else {
        // storable.setOriginalEstimate(dataItem.getOriginalEstimate());
        // }
        // }
        storable.setPriority(dataItem.getPriority());
        storable.setState(dataItem.getState());

        // if(dataItem.getState() == State.DONE) {
        // storable.setEffortLeft(new AFTime(0));
        // } else if(dataItem.getEffortLeft() == null) {
        // storable.setEffortLeft(storable.getOriginalEstimate());
        // }

        if (storable.getBacklog() != null && storable.getBacklog() != backlog) {
            this.moveStoryToBacklog(storable, backlog);
            historyUpdated = true;
        } else if (storable.getBacklog() == null) {
            storable.setBacklog(backlog);
        }

        storable.getResponsibles().clear();
        storable.getResponsibles().addAll(responsibles);

        Story persisted;

        if (storable.getId() == 0) {
            int persistedId = (Integer) storyDAO.create(storable);
            persisted = storyDAO.get(persistedId);
        } else {
            storyDAO.store(storable);
            persisted = storable;
        }
        // if(!historyUpdated) {
        // historyBusiness.updateBacklogHistory(backlog.getId());
        // }
        return persisted;
    }

    public void moveStoryToBacklog(Story story, Backlog backlog) {

        Backlog oldBacklog = story.getBacklog();
        oldBacklog.getStories().remove(story);
        story.setBacklog(backlog);
        backlog.getStories().add(story);
        // historyBusiness.updateBacklogHistory(oldBacklog.getId());
        // historyBusiness.updateBacklogHistory(backlog.getId());

        // if(!backlogBusiness.isUnderSameProduct(oldBacklog, backlog)) {
        // //remove only product themes
        // Collection<BusinessTheme> removeThese = new
        // ArrayList<BusinessTheme>();;
        // for(BusinessTheme theme : story.getBusinessThemes()) {
        // if(!theme.isGlobal()) {
        // removeThese.add(theme);
        // }
        // }
        // for(BusinessTheme theme : removeThese) {
        // story.getBusinessThemes().remove(theme);
        // }
        // }
    }

    public void setBacklogDAO(BacklogDAO backlogDAO) {
        this.backlogDAO = backlogDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setIterationDAO(IterationDAO iterationDAO) {
        this.iterationDAO = iterationDAO;
    }
    public StoryMetrics calculateMetrics(Story story) {
        StoryMetrics metrics = new StoryMetrics();
        int tasks = 0;
        int doneTasks = 0;
        for (Task task : story.getTasks()) {
            if (task.getOriginalEstimate() != null) {
                metrics.setOriginalEstimate(metrics.getOriginalEstimate()
                        + task.getOriginalEstimate().getMinorUnits());
            }
            if (task.getEffortLeft() != null) {
                metrics.setEffortLeft(metrics.getEffortLeft()
                        + task.getEffortLeft().getMinorUnits());
            }
            tasks += 1;
            if (task.getState() == State.DONE) {
                doneTasks += 1;
            }
        }
        metrics.setDoneTasks(doneTasks);
        metrics.setTotalTasks(tasks);
        return metrics;
    }

    @Transactional(readOnly = true)
    public StoryMetrics calculateMetrics(int storyId) {
        return storyDAO.calculateMetrics(storyId);
    }

    @Transactional(readOnly = true)
    public StoryMetrics calculateMetricsWithoutStory(int iterationId) {
        return storyDAO.calculateMetricsWithoutStory(iterationId);
    }

}