Gs-Collections-Kata-Boot
===

This project aims to offer an efficient support to boot Gs-Collection code kata. Using tasks categorized at **Gs-Collections-Kata**, you can get trained in Gs-Collections, without any bothering tasks like downloading the zip file, un-archiving it or importing project into your IDEs.

Usage
===

1. clone this repository
1. run IDE task(ex. for eclipse `gradle eclipse`/ for IntelliJ `gradle idea`)
1. run gradle task named `prepareKata`
1. run gradle task named `exercise1`
1. code to pass the test `Exercise1Test.java`
1. run gradle task named `exercise2`
1. code...

If you edit original files by mistake, please run gradle task named `deleteTemplate`, and run `prepareKata` task again.

If you finished all exercises, and want more training, please run gradle task named `resetKata` which will delete all exercise files from source sets, then run task `prepareKata` again.

---

All rights of build sources is reserved to Shinya Mochida. 

