package com.sueldoneto.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sueldoneto.core.ContractType
import com.sueldoneto.core.DismissalType
import java.time.LocalDate

/** Lo escrito en cada calculadora, para no perderlo al cambiar de pestaña. */
class Forms {
    val salary = SalaryForm()
    val compare = CompareForm()
    val unemployment = UnemploymentForm()
    val severance = SeveranceForm()
}

class SalaryForm {
    var gross by mutableStateOf("")
    var payments by mutableIntStateOf(14)
    var contract by mutableStateOf(ContractType.PERMANENT)
    var children by mutableIntStateOf(0)
    var childrenUnder3 by mutableIntStateOf(0)
}

class CompareForm {
    var grossA by mutableStateOf("")
    var grossB by mutableStateOf("")
    var payments by mutableIntStateOf(14)
    var children by mutableIntStateOf(0)
}

class UnemploymentForm {
    var monthlyBase by mutableStateOf("")
    var daysContributed by mutableStateOf("")
    var children by mutableIntStateOf(0)
}

class SeveranceForm {
    var gross by mutableStateOf("")
    var start by mutableStateOf<LocalDate?>(null)
    var end by mutableStateOf(LocalDate.now())
    var type by mutableStateOf(DismissalType.UNFAIR)
    var vacationDays by mutableStateOf("")
}
