package com.example.employee_management.service;

import com.example.employee_management.EmployeeServiceImpl;
import com.example.employee_management.entity.Employee;
import com.example.employee_management.exception.EmployeeNotFoundException;
import com.example.employee_management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul@example.com")
                .department("Engineering")
                .salary(55000.0)
                .phone("9876543210")
                .build();
    }

    @Test
    @DisplayName("saveEmployee: should save and return employee when email is unique")
    void saveEmployee_success() {
        when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee saved = employeeService.saveEmployee(employee);

        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("rahul@example.com");
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    @DisplayName("saveEmployee: should throw when email already exists")
    void saveEmployee_duplicateEmail_throws() {
        when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.saveEmployee(employee))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllEmployees: should return list from repository")
    void getAllEmployees_returnsList() {
        Employee second = Employee.builder().id(2L).firstName("Priya").lastName("Verma")
                .email("priya@example.com").department("HR").salary(45000.0).build();

        when(employeeRepository.findAll()).thenReturn(Arrays.asList(employee, second));

        List<Employee> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("Rahul");
    }

    @Test
    @DisplayName("getEmployeeById: should return employee when found")
    void getEmployeeById_found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Rahul");
    }

    @Test
    @DisplayName("getEmployeeById: should throw EmployeeNotFoundException when not found")
    void getEmployeeById_notFound_throws() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateEmployee: should update fields and save")
    void updateEmployee_success() {
        Employee updated = Employee.builder()
                .firstName("Rahul").lastName("Verma")
                .email("rahul.verma@example.com")
                .department("Senior Engineering").salary(75000.0).phone("9999999999")
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.updateEmployee(1L, updated);

        assertThat(result.getLastName()).isEqualTo("Verma");
        assertThat(result.getSalary()).isEqualTo(75000.0);
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("deleteEmployee: should call repository delete when exists")
    void deleteEmployee_success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteEmployee: should throw when id not found")
    void deleteEmployee_notFound_throws() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    @DisplayName("getEmployeeByEmail: should return employee when found")
    void getEmployeeByEmail_found() {
        when(employeeRepository.findByEmail("rahul@example.com")).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeByEmail("rahul@example.com");

        assertThat(result.getEmail()).isEqualTo("rahul@example.com");
    }
}