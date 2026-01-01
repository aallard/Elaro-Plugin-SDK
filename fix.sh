sed -i '' 's/1.0.0-SNAPSHOT/1.0.0/g' pom.xml
git add -A && git commit -m "release: v1.0.0"
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin main --tags
