git tag -d $1
git push origin :refs/tags/$1
git tag -a $1 -m "version $1"
git push origin --tags

