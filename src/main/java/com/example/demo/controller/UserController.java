//package com.example.demo.controller;
//
//import com.example.demo.entity.User;
//import com.example.demo.core.login.modal.request.LoginRequest;
//import com.example.demo.core.login.modal.request.response.ResponseObject;
//import com.example.demo.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/login")
//@CrossOrigin("*")
//public class UserController {
//    @Autowired
//    private UserService userService;
//    @PostMapping
//    public ResponseObject<?> loginn(@RequestBody LoginRequest loginRequest) {
//        Optional<User> user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
//
//        if (user.isPresent()) {
//            return new ResponseObject(200, "success");
//        } else {
//            return new ResponseObject(400, "fail");
//        }
//    }
//    @GetMapping("/list")
//    public ResponseObject<List<User>> getAll(){
//        List<User> users = userService.getAll();
//        return new ResponseObject<>(users,"Lấy Danh Sách user thành công");
//    }
//}
