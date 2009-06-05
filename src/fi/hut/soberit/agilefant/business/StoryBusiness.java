package fi.hut.soberit.agilefant.business;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Story;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.util.ResponsibleContainer;
import fi.hut.soberit.agilefant.util.StoryMetrics;

public interface StoryBusiness extends GenericBusiness<Story> {

    Story store(int storyId, int backlogId, Story dataItem,
            Set<Integer> responsibles) throws ObjectNotFoundException;

    public List<Story> getStoriesByBacklog(Backlog backlog);

    /**
     * Get the story's tasks as <code>StoryData</code>.
     * <p>
     * If <code>story</code> is null, return tasks without story.
     */
    public Collection<Task> getStoryContents(Story story, Iteration iteration);
    
    public Collection<ResponsibleContainer> getStoryResponsibles(Story story);

    StoryMetrics calculateMetrics(int storyId);

    StoryMetrics calculateMetricsWithoutStory(int iterationId);

    StoryMetrics calculateMetrics(Story story);

}