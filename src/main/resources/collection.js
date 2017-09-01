
var view = new Vue({
    el: '#elements',
    data: {
        title: "",
        elems: [],
        colType: ""
    },
    created() {
        var colType = (new URL(location).searchParams).get('type');
        this.colType = colType;
        switch (colType) {
            case "users": colUrl = "/users"; break;
            case "sports": colUrl = "/sports"; break;
        }
        fetch(colUrl)
            .then ( (response) => { return response.json(); } )
            .then (
                (json) => {
                    this.title = json.title;
                    json.data.forEach (piece => {
                        piece.colType = this.colType;
                        this.elems.push( piece );
                    } )
                }
            )
    }
});
