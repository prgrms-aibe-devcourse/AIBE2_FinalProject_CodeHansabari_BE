package com.cvmento.domain.resume.seed;

import com.cvmento.domain.resume.entity.TechStack;
import com.cvmento.domain.resume.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 기술 스택 데이터 시더
 * 애플리케이션 시작 시 기본 기술 스택 데이터를 데이터베이스에 삽입
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TechStackSeeder {

    private final TechStackRepository techStackRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void seedTechStacks() {
        if (techStackRepository.count() == 0) {
            log.info("기술 스택 데이터 시딩 시작");
            List<TechStack> techStacks = createTechStacks();
            techStackRepository.saveAll(techStacks);
            log.info("기술 스택 데이터 시딩 완료 - 총 {}개", techStacks.size());
        } else {
            log.info("기술 스택 데이터가 이미 존재합니다. 시딩을 건너뜁니다.");
        }
    }

    private List<TechStack> createTechStacks() {
        List<TechStack> techStacks = new ArrayList<>();

        // Language (프로그래밍 언어)
        String[] languages = {
                "JavaScript", "TypeScript", "Java", "Python", "C", "C++", "C#", "Go", "Rust",
                "Swift", "Kotlin", "PHP", "Ruby", "Scala", "Clojure", "Elixir", "Erlang",
                "Haskell", "F#", "Crystal", "Dart", "Julia", "R", "Lua", "Groovy", "Hack",
                "Nim", "Vala", "AWK", "CUE", "Elm", "VBA"
        };
        addTechStacks(techStacks, languages, "Language");

        // Frontend
        String[] frontend = {
                "HTML5", "CSS3", "React", "Vue.js", "Svelte", "Next.js", "Nuxt.js", "Gatsby",
                "Ember.js", "jQuery", "Vanilla JS", "Material-UI", "Styled Components",
                "Tailwind CSS", "SCSS", "LESS", "Sass", "Foundation", "JSX", "Handlebars",
                "Pug", "Three.js", "WebGL", "SVG", "Vuetify"
        };
        addTechStacks(techStacks, frontend, "Frontend");

        // Backend
        String[] backend = {
                "Node.js", "Express.js", "NestJS", "Fastify", "Koa", "Spring", "Spring Boot",
                "Django", "Flask", "FastAPI", "Laravel", "Ruby on Rails", "Phoenix", "Sinatra",
                "CakePHP", "Symfony", "Lumen", "Drupal", "WordPress", "Strapi", "Hibernate",
                "JPA", "Thymeleaf", "Tomcat", "Entity Framework"
        };
        addTechStacks(techStacks, backend, "Backend");

        // Database
        String[] database = {
                "MySQL", "PostgreSQL", "MongoDB", "Redis", "MariaDB", "Oracle", "SQLite",
                "Cassandra", "CouchDB", "Couchbase", "TiDB", "SQL", "NoSQL", "Memcached",
                "CoreData", "Mongoose"
        };
        addTechStacks(techStacks, database, "Database");

        // DevOps
        String[] devops = {
                "Docker", "Kubernetes", "Jenkins", "CircleCI", "Travis CI", "AWS", "GCP",
                "Heroku", "Netlify", "Vercel", "Terraform", "Helm", "Git", "GitHub", "GitLab",
                "SVN", "Chef", "Nginx", "NGINX", "PM2", "K8S", "K3d", "Kudu", "k6", "Kong",
                "Zuul"
        };
        addTechStacks(techStacks, devops, "DevOps");

        // Mobile
        String[] mobile = {
                "React Native", "Flutter", "iOS", "Ionic", "Cordova", "Unity", "Cocos2d",
                "Rax", "Flet"
        };
        addTechStacks(techStacks, mobile, "Mobile");

        // Testing
        String[] testing = {
                "Jest", "JUnit", "Mocha", "Chai", "Cypress", "Selenium", "Jasmine", "Karma",
                "Protractor", "TestCafe", "Cucumber", "Puppeteer", "TDD", "Moq"
        };
        addTechStacks(techStacks, testing, "Testing");

        // Build/Package
        String[] buildPackage = {
                "Webpack", "Gradle", "Maven", "npm", "Yarn", "Bower", "Grunt", "Gulp", "Babel",
                "Vite", "Rollup", "Parcel", "Lerna", "Rush", "Buck", "Bit", "ESLint",
                "SonarQube", "PMD", "Dotenv"
        };
        addTechStacks(techStacks, buildPackage, "Build/Package");

        // Design/UI
        String[] design = {
                "Figma", "Photoshop", "Sketch", "Adobe XD", "IntelliJ IDEA", "Visual Studio",
                "Eclipse", "Sublime Text", "Vim", "UXCam", "UXPin", "DaVinci"
        };
        addTechStacks(techStacks, design, "Design/UI");

        // Data/Analytics
        String[] dataAnalytics = {
                "Pandas", "TensorFlow", "PyTorch", "Hadoop", "Kafka", "ELK", "Machine Learning",
                "MATLAB", "Pig", "H2O", "NLP", "OLAP", "ETL", "DVC", "BI", "Google Analytics"
        };
        addTechStacks(techStacks, dataAnalytics, "Data/Analytics");

        // Protocol/Standard
        String[] protocol = {
                "HTTP", "REST", "GraphQL", "WebSocket", "WebRTC", "OAuth", "JWT", "JSON",
                "XML", "YAML", "OpenAPI", "XMPP", "SSL", "SSH", "LDAP", "IPFS"
        };
        addTechStacks(techStacks, protocol, "Protocol/Standard");

        // Architecture/Pattern
        String[] architecture = {
                "MSA", "MVC", "MVVM", "DDD", "EDA", "SPA", "PWA", "Flux", "Redux", "MobX",
                "SWR", "OWIN"
        };
        addTechStacks(techStacks, architecture, "Architecture/Pattern");

        // Hardware/System
        String[] hardware = {
                "ARM", "CUDA", "FPGA", "MCU", "Linux", "Ubuntu", "WSL", "VR", "AR", "Qt",
                "Qemu", "Xen", "L2", "L3", "L4", "L7", "RF", "HW", "FW", "SW"
        };
        addTechStacks(techStacks, hardware, "Hardware/System");

        // Enterprise/Business
        String[] enterprise = {
                "SAP", "ERP", "MES", "ISMS", "Confluence", "Jira", "Trac", "Okta", "Zoho",
                "Zoom", "Box", "Dropbox", "OVH", "Wix", "Yii"
        };
        addTechStacks(techStacks, enterprise, "Enterprise/Business");

        // Game/Media
        String[] gameMedia = {
                "Unity", "Godot", "Cocos2d", "Unreal Engine", "OpenGL", "OpenCV", "DaVinci"
        };
        addTechStacks(techStacks, gameMedia, "Game/Media");

        // Blockchain/Crypto
        String[] blockchain = {
                "Truffle", "Eos", "Nft", "DID"
        };
        addTechStacks(techStacks, blockchain, "Blockchain/Crypto");

        // Other
        String[] other = {
                "Meteor", "Hexo", "Hugo", "Firebase", "Prisma", "Passport.js", "Socket.io",
                "Lodash", "Dojo", "Iron", "Iris", "hub", "Q.js", "QA", "ROS", "RPA", "SDK",
                "SEO", "Swagger", "TCAD", "Tilt", "Trax", "Uppy", "Utm", "WPF", "Wey",
                "ws", "8x8", "Yoga", "Yolk", "yolo", "ZK", "Zest", "Zeta", "pip", "FF4J",
                "IPS", "NSQ", "Npl", "Ora", "Que", "Quuu", "RDB", "Afi", "act", "Dm",
                "Dw", "DB", "4D", "Gym", "Gum", "Vuo", "Leaflet", "Postman", "CloudFlare",
                "CoffeeScript", "Electron"
        };
        addTechStacks(techStacks, other, "Other");

        return techStacks;
    }

    private void addTechStacks(List<TechStack> techStacks, String[] names, String category) {
        for (String name : names) {
            techStacks.add(TechStack.createTechStack(name, category));
        }
    }
}