package com.mojang.rubydung.ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class World {

    private final List<Entity> entities = new ArrayList<>();
    private final Map<Class<? extends Component>, Map<Integer, Component>> componentStores = new HashMap<>();
    private final List<System> systems = new ArrayList<>();

    public Entity createEntity() {
        Entity entity = new Entity();
        this.entities.add(entity);
        return entity;
    }

    public void addComponent(Entity entity, Component component) {
        this.componentStores.computeIfAbsent(component.getClass(), k -> new HashMap<>()).put(entity.id, component);
    }

    public <T extends Component> T getComponent(Entity entity, Class<T> componentClass) {
        Map<Integer, Component> store = this.componentStores.get(componentClass);
        if (store == null) {
            return null;
        }
        return componentClass.cast(store.get(entity.id));
    }

    public <T extends Component> boolean hasComponent(Entity entity, Class<T> componentClass) {
        Map<Integer, Component> store = this.componentStores.get(componentClass);
        return store != null && store.containsKey(entity.id);
    }

    public void addSystem(System system) {
        this.systems.add(system);
    }

    public <T extends System> T getSystem(Class<T> systemClass) {
        for (System system : this.systems) {
            if (systemClass.isInstance(system)) {
                return systemClass.cast(system);
            }
        }
        return null;
    }

    public List<Entity> getEntitiesWith(Class<? extends Component>... componentClasses) {
        return this.entities.stream()
                .filter(entity -> {
                    for (Class<? extends Component> componentClass : componentClasses) {
                        if (!hasComponent(entity, componentClass)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public void update(float partialTicks) {
        for (System system : this.systems) {
            system.update(this, partialTicks);
        }
    }
}
