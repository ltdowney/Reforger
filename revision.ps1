

git rev-list --abbrev-commit HEAD | Measure-Object -line | %{ $_.Lines; }

