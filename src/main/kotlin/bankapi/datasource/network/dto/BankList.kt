package bankapi.datasource.network.dto

import bankapi.model.Bank

data class BankList(
    val results: Collection<Bank>
)