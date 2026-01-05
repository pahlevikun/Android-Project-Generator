#!/usr/bin/env node

const { Command } = require('commander');
const fs = require('fs-extra');
const path = require('path');
const ejs = require('ejs');
const inquirer = require('inquirer');
const shell = require('shelljs');
const chalk = require('chalk');

async function getPrompt() {
  const m = inquirer && typeof inquirer === 'object' ? inquirer : {};
  if (typeof m.prompt === 'function') return m.prompt.bind(m);
  if (m.default && typeof m.default.prompt === 'function') return m.default.prompt.bind(m.default);
  try {
    const mod = await import('inquirer');
    const x = mod.default || mod;
    if (typeof x.prompt === 'function') return x.prompt.bind(x);
  } catch {}
  return null;
}

const program = new Command();

program
  .version('1.0.0')
  .description('Android Project Generator following Clean Architecture')
  .argument('<packageName>', 'Package name (e.g. com.example.app)')
  .argument('<appName>', 'Application name (e.g. MyApp)')
  .option('--firebase', 'Include Firebase dependencies')
  .option('--no-firebase', 'Skip Firebase dependencies')
  .option('--flavors <list>', 'Comma-separated product flavors', 'staging,production')
  .option('--flavor-dimension <name>', 'Flavor dimension name', 'environment')
  .action(async (packageName, appName, options) => {
    try {
      // Validation
      const packageNameRegex = /^[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]$/;
      if (!packageNameRegex.test(packageName)) {
        console.error(chalk.red('Error: Invalid package name. It must follow Java package naming conventions.'));
        process.exit(1);
      }

      let useFirebase = options.firebase;

      if (useFirebase === undefined) {
          const prompt = await getPrompt();
          if (prompt) {
            const answers = await prompt([
              {
                type: 'confirm',
                name: 'useFirebase',
                message: 'Do you want to include Firebase dependencies?',
                default: false,
              },
            ]);
            useFirebase = answers.useFirebase;
          } else {
            console.log(chalk.yellow('Inquirer not available; defaulting to no Firebase. Use --firebase to enable.'));
            useFirebase = false;
          }
      }
      const projectDir = path.join(process.cwd(), appName);

      if (fs.existsSync(projectDir)) {
        console.error(chalk.red(`Error: Directory ${appName} already exists.`));
        process.exit(1);
      }

      console.log(chalk.blue(`Generating project ${appName} with package ${packageName}...`));

      // 1. Create Project Root
      fs.mkdirSync(projectDir);

      // 2. Copy Root Templates
      const templateRoot = path.join(__dirname, 'templates/root');
      const rootFiles = fs.readdirSync(templateRoot);

      for (const file of rootFiles) {
        const srcPath = path.join(templateRoot, file);
        const destPath = path.join(projectDir, file);

        if (fs.lstatSync(srcPath).isDirectory()) {
          fs.copySync(srcPath, destPath);
        } else {
          renderTemplate(srcPath, destPath, { packageName, appName, useFirebase });
        }
      }

      const gradlewPath = path.join(projectDir, 'gradlew');
      if (fs.existsSync(gradlewPath)) {
        try { fs.chmodSync(gradlewPath, 0o755); } catch {}
      }

      // 3. Create App Module
      const appDir = path.join(projectDir, 'app');
      fs.mkdirSync(appDir);

      // 4. App Build Gradle
      const flavors = (options.flavors || 'staging,production').split(',').map(s => s.trim()).filter(Boolean);
      const flavorDimension = options.flavorDimension || 'environment';
      renderTemplate(
        path.join(__dirname, 'templates/app/build.gradle.kts'),
        path.join(appDir, 'build.gradle.kts'),
        { packageName, appName, useFirebase, flavors, flavorDimension }
      );

      // 5. Source Directory Structure
      const packagePath = packageName.replace(/\./g, '/');
      const srcMainJava = path.join(appDir, 'src/main/java', packagePath);
      const srcMainRes = path.join(appDir, 'src/main/res');

      fs.ensureDirSync(srcMainJava);
      fs.ensureDirSync(srcMainRes);

      // 6. Create Clean Architecture Packages
      const layers = ['data', 'domain', 'presentation', 'di', 'core'];
      layers.forEach(layer => {
        fs.ensureDirSync(path.join(srcMainJava, layer));
      });

      // 7. Render Kotlin Files
      const kotlinTemplates = path.join(__dirname, 'templates/app/src/main/java');
      
      // MainApplication
      renderTemplate(
        path.join(kotlinTemplates, 'MainApplication.kt'),
        path.join(srcMainJava, 'MainApplication.kt'),
        { packageName, appName }
      );

      // MainActivity
      renderTemplate(
        path.join(kotlinTemplates, 'MainActivity.kt'),
        path.join(srcMainJava, 'MainActivity.kt'),
        { packageName, appName }
      );

      // DI Module
      fs.ensureDirSync(path.join(srcMainJava, 'di'));
      renderTemplate(
        path.join(kotlinTemplates, 'di/AppModule.kt'),
        path.join(srcMainJava, 'di/AppModule.kt'),
        { packageName, appName }
      );

      // Theme
      const themeDir = path.join(srcMainJava, 'presentation/theme');
      fs.ensureDirSync(themeDir);
      ['Theme.kt', 'Color.kt', 'Type.kt'].forEach(file => {
         renderTemplate(
            path.join(kotlinTemplates, 'presentation/theme', file),
            path.join(themeDir, file),
            { packageName, appName }
         );
      });

      // 8. Render AndroidManifest
      renderTemplate(
        path.join(__dirname, 'templates/app/src/main/AndroidManifest.xml'),
        path.join(appDir, 'src/main/AndroidManifest.xml'),
        { packageName, appName }
      );

      // 9. Copy Resources (xml, mipmap if I had them, values)
      const resTemplates = path.join(__dirname, 'templates/app/src/main/res');
      fs.copySync(resTemplates, srcMainRes);
      
      // Re-render strings.xml and themes.xml because they have placeholders
      renderTemplate(
        path.join(resTemplates, 'values/strings.xml'),
        path.join(srcMainRes, 'values/strings.xml'),
        { appName }
      );
      renderTemplate(
        path.join(resTemplates, 'values/themes.xml'),
        path.join(srcMainRes, 'values/themes.xml'),
        { appName }
      );


      // 10. Git Ignore
      const gitignore = `
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
`;
      fs.writeFileSync(path.join(projectDir, '.gitignore'), gitignore);
      fs.writeFileSync(path.join(appDir, '.gitignore'), '/build\n');

      // 11. Final Structure Validation
      const requiredFiles = [
        path.join(projectDir, 'build.gradle.kts'),
        path.join(projectDir, 'settings.gradle.kts'),
        path.join(appDir, 'build.gradle.kts'),
        path.join(appDir, 'src/main/AndroidManifest.xml'),
        path.join(srcMainJava, 'MainActivity.kt'),
        path.join(srcMainJava, 'MainApplication.kt'),
        path.join(srcMainJava, 'di/AppModule.kt')
      ];

      const missingFiles = requiredFiles.filter(f => !fs.existsSync(f));
      if (missingFiles.length > 0) {
        throw new Error(`Validation failed. Missing files: ${missingFiles.join(', ')}`);
      }

      console.log(chalk.green(`\nSuccess! Created ${appName} at ${projectDir}`));
      
      if (useFirebase) {
         console.log(chalk.yellow(`\nTODO: Don't forget to add your google-services.json to ${path.join(appDir)}`));
      }
      
      console.log(chalk.cyan(`\nTo get started:`));
      console.log(chalk.cyan(`  cd ${appName}`));
      console.log(chalk.cyan(`  ./gradlew assembleDebug`));

    } catch (error) {
      console.error(chalk.red('An error occurred:'), error);
      process.exit(1);
    }
  });

function renderTemplate(src, dest, data) {
  const content = fs.readFileSync(src, 'utf-8');
  const rendered = ejs.render(content, data);
  fs.writeFileSync(dest, rendered);
}

program.parse(process.argv);
