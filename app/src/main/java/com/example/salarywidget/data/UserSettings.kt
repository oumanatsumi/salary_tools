package com.example.salarywidget.data

/**
 * 用户薪资设置数据类
 * 存储所有用户输入的配置信息
 */
data class UserSettings(
    // 薪资模式: PRE_TAX=税前, POST_TAX=税后到手
    val salaryMode: SalaryMode = SalaryMode.PRE_TAX,

    // 月薪金额（税前或税后，取决于 salaryMode）
    val monthlySalary: Double = 0.0,

    // 工作时间
    val workStartHour: Int = 9,
    val workStartMinute: Int = 0,
    val workEndHour: Int = 18,
    val workEndMinute: Int = 0,

    // 午休时间
    val lunchStartHour: Int = 12,
    val lunchStartMinute: Int = 0,
    val lunchEndHour: Int = 13,
    val lunchEndMinute: Int = 0,

    // 五险一金模式
    val insuranceMode: InsuranceMode = InsuranceMode.FIXED_AMOUNT,

    // 固定金额模式下的每月扣除金额
    val insuranceFixedAmount: Double = 0.0,

    // 比例模式下的各项比例（小数形式，如 0.08 表示 8%）
    val pensionRate: Double = 0.08,        // 养老保险
    val medicalRate: Double = 0.02,        // 医疗保险
    val unemploymentRate: Double = 0.005,  // 失业保险
    val housingFundRate: Double = 0.12,    // 住房公积金

    // 每月工作天数（默认中国标准 21.75）
    val workingDaysPerMonth: Double = 21.75,

    // 是否已完成初始设置
    val isConfigured: Boolean = false
)

/**
 * 薪资计算模式
 */
enum class SalaryMode {
    PRE_TAX,   // 税前月薪（需要扣除五险一金）
    POST_TAX   // 税后到手月薪（不再扣除五险一金）
}

/**
 * 五险一金输入模式
 */
enum class InsuranceMode {
    FIXED_AMOUNT,  // 固定金额
    RATE           // 比例模式
}
