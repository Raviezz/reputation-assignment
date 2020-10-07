import React from 'react';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Container from '@material-ui/core/Container';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import { withStyles } from '@material-ui/core/styles';
import { navService } from './services/nav.service';
import JSONPretty from 'react-json-pretty';
import { JSONPrettyMon } from 'react-json-pretty/themes/monikai.css';
import yourJSON from './mock.data'; // use mock data incase u get CORS exception

const useStyles = theme => ({
    root: {
        flexGrow: 1,
    },
    menuButton: {
        marginRight: theme.spacing(2),
    },
    title: {
        flexGrow: 1,
    },
    maincontent: {
        flexGrow: 1,
        marginTop: 100
    },
    paper: {
        padding: theme.spacing(5),
        textAlign: 'center',
        color: theme.palette.text.secondary,
        height: 300,
        overflow: 'auto'
    },
    form: {
        width: '100%', // Fix IE 11 issue.
        marginTop: theme.spacing(1),
        justifyContent: 'center'
    },
    reset: {
        marginTop: 10,
        '& > *': {
            margin: theme.spacing(0.5),
        },
    },
});

class Home extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            scheme: '',
            period: 0,
            horizon: 0,
            submitted: false,
            dataset: {}
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }
    handleChange = (e) => {
        const { name, value } = e.target;
        this.setState({ [name]: value });
    }


    handleSubmit = (e) => {
        e.preventDefault();
        this.setState({ submitted: true })
        const { scheme, period, horizon } = this.state;

        if (scheme && period && horizon) {
            console.log("Fetching data for", scheme, period, horizon);
            navService.fetchNetAssetValueReturns(scheme, period, horizon).then(response => {
                console.log("After call", response);
                this.setState({ dataset: response });
            });
        }


    }

    handleReset = () => {
        this.setState({ scheme: '', period: 0, horizon: 0, submitted: false });
    }
    render() {
        const { classes } = this.props;

        return (
            <div className={classes.root}>
                <AppBar position="static">
                    <Toolbar>
                        <Typography variant="h6" className={classes.title}>
                            Reputation.com
                     </Typography>

                    </Toolbar>
                </AppBar>
                <Container fixed>
                    <div className={classes.maincontent}>
                        <Grid container spacing={3}>

                            <Grid item xs={6}>
                                <Paper className={classes.paper}>
                                    <form className={classes.form} noValidate onSubmit={this.handleSubmit}>
                                        <TextField
                                            variant="outlined"
                                            margin="normal"
                                            value={this.state.scheme}
                                            required
                                            fullWidth
                                            id="scheme"
                                            label="Scheme number"
                                            name="scheme"
                                            autoComplete="scheme"
                                            autoFocus
                                            onChange={this.handleChange}
                                        />
                                        <TextField
                                            variant="outlined"
                                            margin="normal"
                                            value={this.state.period}
                                            required
                                            fullWidth
                                            name="period"
                                            label="Investment Period"
                                            type="number"
                                            id="period"
                                            autoComplete="current-password"
                                            onChange={this.handleChange}
                                        />
                                        <TextField
                                            variant="outlined"
                                            margin="normal"
                                            value={this.state.horizon}
                                            required
                                            fullWidth
                                            name="horizon"
                                            label="Horizon"
                                            type="number"
                                            id="password"
                                            autoComplete="current-password"
                                            onChange={this.handleChange}
                                        />

                                        <Button
                                            type="submit"
                                            fullWidth
                                            variant="contained"
                                            color="primary"
                                            className={classes.submit}
                                            disabled={this.state.submitted}
                                        >
                                            Submit
                                    </Button>
                                        <Button className={classes.reset} variant="contained" onClick={this.handleReset}>Reset</Button>
                                    </form>
                                </Paper>
                            </Grid>
                            <Grid item xs={6}>
                                <Paper className={classes.paper}>
                                    <JSONPretty data={this.state.dataset} theme={JSONPrettyMon}></JSONPretty>
                                </Paper>
                            </Grid>

                        </Grid>
                    </div>
                </Container>
            </div>
        );
    }
}

export default withStyles(useStyles, { withTheme: true })(Home);