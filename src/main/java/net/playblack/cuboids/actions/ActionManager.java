package net.playblack.cuboids.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.playblack.cuboids.actions.events.CuboidEvent;
import net.playblack.cuboids.exceptions.InvalidActionHandlerException;
import net.playblack.mcutils.ToolBox;

/**
 * This class handles firing action events all over the place,
 * invoking listening actions if required. You may also register your thing here
 * @author chris
 *
 */
public class ActionManager {
    HashMap<Class<? extends CuboidEvent>, Set<RegisteredAction>> actions;
    
    private static ActionManager instance;
    
    private ActionManager() {
        
    }
    
    public static void fireEvent(CuboidEvent event) {
        Set<RegisteredAction> receivers = ActionManager.instance.actions.get(event.getClass().asSubclass(CuboidEvent.class));
        for(RegisteredAction action : receivers) {
            action.execute(event);
        }
    }
    
    /**
     * Register your {@link ActionListener} here to make it available for Cuboid2's event system
     * @param listener
     * @return
     * @throws InvalidActionHandlerException if the signature of an actionhandler annotated method is incorrect
     */
    public static void registerActionListener(String owner, ActionListener listener) throws InvalidActionHandlerException {
        //Make a new instance if there is none
        if(instance == null) {
            instance = new ActionManager();
        }
        //Here comes fancy-pancy reflection magic
        //Note: All the final stuff right here is to make sure we can use the data
        //in the inline declaration for ActionExecutor.
        //Props and thx and Kudos to the Bukkit folks for I took some pages out of their book (JavaPluginLoader)
        
        Method[] allMethods = ToolBox.safeMergeArrays(listener.getClass().getMethods(), listener.getClass().getDeclaredMethods());
        //First check the public methods for Actionhandler annotations
        for(final Method m : allMethods) {
            final ActionHandler handler = m.getAnnotation(ActionHandler.class);
            if(handler == null) { continue; } //not an action handling method, bye
            //Check if the new method has correct number of parameters (1)
            if(m.getParameterTypes().length != 1) {
                throw new InvalidActionHandlerException(owner + "tried to register action handler with invalid signature! Wrong num parameters for " + m.getName());
            }
            //If we have 1 parameter, check if it is of the correct type
            final Class<?> eventClass = m.getParameterTypes()[0];
            if(!CuboidEvent.class.isAssignableFrom(eventClass)) {
                throw new InvalidActionHandlerException(owner + "tried to register action handler with invalid signature! Wrong parameter type for " + m.getName());
            }
            //Okay, we're cool. Lets try to register that thing!
            //Make sure we have a working set for registered actions before adding it.
            instance.registerEventType(eventClass.asSubclass(CuboidEvent.class));
            
            ActionExecutor executor = new ActionExecutor() {
                
                @Override
                public void execute(ActionListener action, CuboidEvent event) {
                    if(eventClass.isAssignableFrom(event.getClass())) {
                        try {
                            m.invoke(action, event);
                        } 
                        catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } 
                        catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } 
                        catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            instance.actions.get(eventClass.asSubclass(CuboidEvent.class)).add(new RegisteredAction(listener, handler.priority(), executor, owner));
        }
    }
    
    /**
     * Check if the event is already registered. If not, it will make a new entry in the HashMap.
     * @param cls
     */
    private void registerEventType(Class<? extends CuboidEvent> cls) {
        if(!actions.containsKey(cls)) {
            actions.put(cls, new HashSet<RegisteredAction>());
        }
    }
}
