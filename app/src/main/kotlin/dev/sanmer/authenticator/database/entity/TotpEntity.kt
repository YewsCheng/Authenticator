package dev.sanmer.authenticator.database.entity

import androidx.room.Entity
import dev.sanmer.authenticator.model.auth.TotpAuth
import dev.sanmer.otp.HOTP

@Entity(tableName = "totp", primaryKeys = ["secret"])
data class TotpEntity(
    val issuer: String,
    val name: String,
    val secret: String,
    val hash: HOTP.Hash,
    val digits: Int,
    val period: Long
) {
    constructor(
        original: TotpAuth
    ) : this(
        issuer = original.issuer,
        name = original.name,
        secret = original.secret,
        hash = original.hash,
        digits = original.digits,
        period = original.period
    )

    val auth get() = TotpAuth(
        issuer = issuer,
        name = name,
        secret = secret,
        hash = hash,
        digits = digits,
        period = period
    )
}
