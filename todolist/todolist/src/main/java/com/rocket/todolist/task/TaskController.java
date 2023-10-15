package com.rocket.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocket.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;
    
    
    @PostMapping("/post")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {

        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        var currentDate = LocalDateTime.now();

        if(currentDate.isBefore(taskModel.getStartedAt()) || currentDate.isBefore(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/término não pode ser anterior/posterior a data atual");
        }

        if(taskModel.getStartedAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início posterior a data de término");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }


    @GetMapping("/list")
    public List<TaskModel> list(HttpServletRequest request){
        return this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));  
    }


    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request){

        var task = this.taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if(task == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada");
        }

        if(!task.getIdUser().equals((UUID) request.getAttribute("idUser"))){
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada");
        }

        Utils.copyNonNullProperties(taskModel, task);


        return ResponseEntity.status(HttpStatus.OK).body(this.taskRepository.save(task));
    }

}
