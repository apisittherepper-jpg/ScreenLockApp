# Screen Orientation Lock Android App 📱

แอพพลิเคชัน Android ตัวอย่างสำหรับการล็อคและควบคุมทิศทางหน้าจอ (Screen Orientation) เฉพาะภายในแอพพลิเคชัน (ไม่กระทบแอพอื่นของระบบ)

## ✨ คุณสมบัติ (Features)
- 🔒 **ล็อคแนวตั้ง (Portrait)**: บังคับให้อยู่ในแนวตั้งตลอดเวลา
- 🔄 **ล็อคแนวนอน (Landscape)**: บังคับให้อยู่ในแนวนอน
- 🌐 **หมุนตามเซนเซอร์ (Sensor Auto)**: หมุนตามทิศทางของอุปกรณ์อัตโนมัติ
- 🔃 **แนวตั้งกลับหัว / แนวนอนกลับด้าน (Reverse Portrait / Landscape)**

## 🚀 การ Build บน GitHub Actions
โปรเจกต์นี้มี CI/CD ด้วย GitHub Actions พร้อมใช้งาน เมื่อ Push โค้ดขึ้น GitHub ระบบจะ Build APK Debug ให้อัตโนมัติ สามารถดาวน์โหลดไฟล์ APK ได้ที่แท็บ **Actions -> Artifacts**

## 🛠 คำสั่ง Build ด้วย Gradle (Local)
```bash
./gradlew assembleDebug
```
ไฟล์ APK จะถูกสร้างอยู่ที่ `app/build/outputs/apk/debug/app-debug.apk`
