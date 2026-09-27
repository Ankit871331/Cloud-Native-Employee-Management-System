package com.example.employee_management.controller;

import com.example.employee_management.entity.Employee;
import com.example.employee_management.exception.EmployeeNotFoundException;
import com.example.employee_management.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    private final String employeeJson = """
            {
              "firstName": "Rahul",
              "lastName": "Sharma",
              "email": "rahul@example.com",
              "department": "Engineering",
              "salary": 55000,
              "phone": "9876543210"
            }
            """;

    @Test
    @DisplayName("POST /api/employees -> 201 Created")
    void createEmployee_returns201() throws Exception {
        Employee saved = Employee.builder().id(1L).firstName("Rahul").lastName("Sharma")
                .email("rahul@example.com").department("Engineering").salary(55000.0)
                .phone("9876543210").build();

        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(saved);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("rahul@example.com"));
    }

    @Test
    @DisplayName("GET /api/employees -> 200 with list")
    void getAllEmployees_returnsList() throws Exception {
        List<Employee> list = Arrays.asList(
                Employee.builder().id(1L).firstName("Rahul").lastName("Sharma").email("r@e.com").department("Eng").salary(50000.0).build(),
                Employee.builder().id(2L).firstName("Priya").lastName("Verma").email("p@e.com").department("HR").salary(45000.0).build()
        );
        when(employeeService.getAllEmployees()).thenReturn(list);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Rahul"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} -> 200 when found")
    void getById_found() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(
                Employee.builder().id(1L).firstName("Rahul").lastName("Sharma").email("r@e.com").department("Eng").salary(50000.0).build()
        );

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/employees/{id} -> 404 when not found")
    void getById_notFound() throws Exception {
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/employees with invalid body -> 400")
    void createEmployee_validationFails() throws Exception {
        String badJson = """
                {
                  "firstName": "",
                  "lastName": "Sharma",
                  "email": "bad-email",
                  "department": "Engineering",
                  "salary": -100
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name is required"))
                .andExpect(jsonPath("$.email").value("Invalid email format"))
                .andExpect(jsonPath("$.salary").value("Salary must be positive"));

        Mockito.verify(employeeService, Mockito.never()).saveEmployee(any());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} -> 204 No Content")
    void deleteEmployee_returns204() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }
}