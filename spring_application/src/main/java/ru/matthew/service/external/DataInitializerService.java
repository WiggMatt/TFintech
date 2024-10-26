package ru.matthew.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.matthew.aop.Timed;
import ru.matthew.command.Command;
import ru.matthew.command.InitializeLocationsCommand;
import ru.matthew.command.InitializePlaceCategoriesCommand;
import ru.matthew.model.Location;
import ru.matthew.model.PlaceCategory;
import ru.matthew.observer.listeners.Observer;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DataInitializerService {
    private final List<Command> commands = new ArrayList<>();

    @Autowired
    public DataInitializerService(ExternalDataLoaderService externalDataLoaderService,
                                  List<Observer<Location>> locationObservers,
                                  List<Observer<PlaceCategory>> placeCategoryObservers) {
        // Создание и добавление команд для инициализации
        commands.add(new InitializeLocationsCommand(externalDataLoaderService, locationObservers));
        commands.add(new InitializePlaceCategoriesCommand(externalDataLoaderService, placeCategoryObservers));
    }

    @Timed
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        log.info("Начало инициализации данных");

        try {
            commands.forEach(Command::execute);
        } catch (Exception e) {
            log.error("Ошибка при инициализации данных", e);
        }
        log.info("Инициализация данных завершена");
    }
}
