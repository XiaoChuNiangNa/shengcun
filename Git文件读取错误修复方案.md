# Git文件读取错误修复方案

## 问题症状
- java.io.FileNotFoundException: 系统找不到指定的路径
- Android Studio在 app/src/main/.git/ 路径下查找Git配置文件失败
- 实际Git仓库位于项目根目录 My-APP3/.git/

## 解决方案

### 方案1：重新关联Git仓库（推荐）

1. **关闭项目**
   - 在Android Studio中关闭当前项目

2. **重新打开项目**
   - File → Open → 选择项目根目录 `D:\Android\My-App\My-APP3`
   - 确保选择的是项目根目录，而不是app子目录

3. **验证Git关联**
   - 打开后检查右下角Git分支显示是否正常
   - 确认Version Control窗口能正常显示Git状态

4. **清理IDE缓存**
   - File → Invalidate Caches / Restart → Invalidate and Restart

### 方案2：手动修复Git配置

1. **检查.idea目录**
   - 删除 `.idea/vcs.xml` 文件
   - 重启Android Studio让其重新检测Git仓库

2. **重新导入Git**
   - VCS → Enable Version Control Integration
   - 选择Git

### 方案3：重置项目配置

1. **备份项目设置**
   ```
   copy .idea\workspace.xml .idea\workspace.xml.backup
   ```

2. **删除IDE配置文件**
   - 删除 `.idea` 目录下的vcs相关配置文件
   - 重启项目重新初始化

## 验证步骤

1. **检查Git状态**
   - 底部状态栏应显示正确的分支名（main）
   - Version Control面板应显示文件变更

2. **测试Git操作**
   - 尝试commit、push等基本Git操作
   - 确认不再出现FileNotFoundException

3. **检查日志**
   - 查看IDE日志确认不再有.git文件读取错误

## 预防措施

1. **项目导入规范**
   - 始终选择包含.git目录的项目根目录导入
   - 避免选择子目录作为项目根目录

2. **定期维护**
   - 定期清理IDE缓存
   - 检查Git关联状态

3. **团队协作**
   - 确保.gitignore正确配置
   - 避免将IDE配置文件提交到版本控制