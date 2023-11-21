package io.springbatchlecture.core.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;

import io.springbatchlecture.dto.PlayerDto;
import io.springbatchlecture.dto.PlayerSalaryDto;
import java.time.Year;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class PlayerSalaryServiceTest {

    private PlayerSalaryService playerSalaryService;

    @BeforeEach
    public void setup() {
        playerSalaryService = new PlayerSalaryService();
    }

    @Test
    public void calcSalary() {
        // given
        /*
        MockedStatic<Year> mockYearClass = Mockito.mockStatic(Year.class);
        Year mockYear = mock(Year.class);
        when(mockYear.getValue()).thenReturn(2023);
        mockYearClass.when(Year::now).thenReturn(mockYear);
        */

        Year mockYear = mock(Year.class);
        when(mockYear.getValue()).thenReturn(2023);
        Mockito.mockStatic(Year.class).when(Year::now).thenReturn(mockYear);

        PlayerDto mockPlayer = mock(PlayerDto.class);
        when(mockPlayer.getBirthYear()).thenReturn(1980);

        // when
        PlayerSalaryDto result = playerSalaryService.calcSalary(mockPlayer);

        // then
        Assertions.assertEquals(result.getSalary(), 43000000);
    }
}